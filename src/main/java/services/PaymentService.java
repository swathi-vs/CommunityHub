package services;

import java.sql.Timestamp;
import java.util.List;

import dao.InvoiceDAO;
import dao.PaymentDAO;
import model.Invoice;
import model.Payment;
import model.ServiceResult;
import utils.Db;
import utils.QueryFilter;

public class PaymentService {

	/**
	 * Records a payment against an invoice and updates the invoice's amount_paid and status in
	 * the same transaction. Rejects payments above the outstanding amount.
	 */
	public static ServiceResult createPayment(Payment payment) {
		payment.setPaidAt(new Timestamp(System.currentTimeMillis()));
		payment.setAmount(Db.money(payment.getAmount()));
		return Db.tx(con -> {
			Invoice invoice = InvoiceDAO.getById(con, payment.getInvoiceId(), payment.getOrganizationId(), true);
			if (invoice == null)
				return ServiceResult.notFound("Invoice");
			if (InvoiceService.CANCELLED.equals(invoice.getStatus()))
				return ServiceResult.conflict("Invoice has been cancelled");
			double total = invoice.getTotalAmount() == null ? 0 : invoice.getTotalAmount();
			double paid = invoice.getAmountPaid() == null ? 0 : invoice.getAmountPaid();
			double outstanding = Db.money(total - paid);
			if (outstanding <= 0)
				return ServiceResult.conflict("Invoice is already fully paid");
			if (payment.getAmount() > outstanding + 0.005)
				return ServiceResult.badRequest("Amount exceeds the outstanding balance of " + outstanding);

			Long id = PaymentDAO.createPayment(con, payment);
			if (id == null)
				return ServiceResult.error();
			double newPaid = Db.money(paid + payment.getAmount());
			String status = InvoiceService.paymentStatus(newPaid, total, invoice.getStatus());
			InvoiceDAO.applyPayment(con, invoice.getId(), newPaid, status);
			payment.setId(id);
			return ServiceResult.ok("Payment recorded", payment);
		});
	}

	public static List<Payment> getPayments(Long orgId, QueryFilter filter) {
		return PaymentDAO.getPayments(orgId, filter);
	}
}
