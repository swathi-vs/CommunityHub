package services;

import java.util.List;

import dao.InvoiceDAO;
import model.Invoice;
import model.ServiceResult;
import model.Tenancy;
import utils.QueryFilter;

public class InvoiceService {

	public static final String PENDING = "pending";
	public static final String PAID = "paid";
	public static final String OVERDUE = "overdue";
	public static final String PARTIAL = "partial";
	public static final String CANCELLED = "cancelled";
	public static final java.util.List<String> TYPES = java.util.Arrays.asList("rent", "deposit", "mess", "utility",
			"penalty", "refund", "other");

	/** Creates an invoice for a tenancy of the property; the invoiced user is the tenancy's tenant. */
	public static ServiceResult createInvoice(Invoice invoice) {
		Tenancy tenancy = TenancyService.getById(invoice.getTenancyId(), invoice.getOrganizationId());
		if (tenancy == null || !tenancy.getPropertyId().equals(invoice.getPropertyId()))
			return ServiceResult.notFound("Tenancy for this property");
		invoice.setUserId(tenancy.getUserId());
		invoice.setStatus(PENDING);
		invoice.setAmountPaid(0.0);
		invoice.setInvoiceNumber("INV-" + invoice.getOrganizationId() + "-" + System.currentTimeMillis());
		Long id = InvoiceDAO.createInvoice(invoice);
		if (id == null)
			return ServiceResult.error();
		invoice.setId(id);
		return ServiceResult.ok("Invoice created", invoice);
	}

	/** Status implied by the amount paid: paid when settled, partial when part-paid. */
	static String paymentStatus(double amountPaid, double total, String current) {
		if (amountPaid >= total - 0.005)
			return PAID;
		if (amountPaid > 0)
			return PARTIAL;
		return PAID.equals(current) || PARTIAL.equals(current) || CANCELLED.equals(current) ? PENDING : current;
	}

	public static List<Invoice> getInvoices(Long orgId, QueryFilter filter) {
		return InvoiceDAO.getInvoices(orgId, filter);
	}

	public static Invoice getById(Long id, Long orgId) {
		return InvoiceDAO.getById(id, orgId);
	}

	/**
	 * Updates an invoice whose fields were merged with the request. requestedStatus is the
	 * status the client asked for (null when not changing it). amount_paid is never edited here.
	 */
	public static ServiceResult updateInvoice(Invoice merged, double amountPaid, String requestedStatus) {
		double total = merged.getTotalAmount() == null ? 0 : merged.getTotalAmount();
		boolean fullyPaid = amountPaid >= total - 0.005;
		if (total + 0.005 < amountPaid)
			return ServiceResult.badRequest("Total amount cannot be less than the amount already paid");
		if (requestedStatus == null || PAID.equals(requestedStatus) || PARTIAL.equals(requestedStatus)) {
			if (PAID.equals(requestedStatus) && !fullyPaid)
				return ServiceResult.badRequest("Invoice is not fully paid. Record a payment instead.");
			if (!CANCELLED.equals(merged.getStatus()) || requestedStatus != null)
				merged.setStatus(paymentStatus(amountPaid, total, merged.getStatus()));
		} else if (PENDING.equals(requestedStatus) && amountPaid > 0) {
			return ServiceResult.badRequest("Invoice already has payments");
		} else if (OVERDUE.equals(requestedStatus) && fullyPaid) {
			return ServiceResult.badRequest("Invoice is fully paid");
		}
		merged.setAmountPaid(amountPaid);
		return InvoiceDAO.updateInvoice(merged) ? ServiceResult.ok("Invoice updated", merged) : ServiceResult.error();
	}
}
