package action;

import java.util.Arrays;
import java.util.List;

import model.Invoice;
import services.InvoiceService;
import utils.ModelMerger;

public class UpdateInvoiceAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private static final List<String> STATUSES = Arrays.asList("pending", "partial", "paid", "overdue", "cancelled");

	private Long organizationId;
	private Invoice invoice = new Invoice();

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice v) {
		this.invoice = v;
	}

	@Override
	public void validate() {
		if (invoice.getId() == null)
			addFieldError("invoice.id", "Invoice ID is missing");
		if (invoice.getStatus() != null && !STATUSES.contains(invoice.getStatus()))
			addFieldError("invoice.status", "Invalid status");
		if (invoice.getInvoiceType() != null && !services.InvoiceService.TYPES.contains(invoice.getInvoiceType()))
			addFieldError("invoice.invoiceType", "Invalid invoice type");
		if (invoice.getTotalAmount() != null && invoice.getTotalAmount() <= 0)
			addFieldError("invoice.totalAmount", "Valid amount required");
	}

	@Override
	public String execute() {
		Invoice existing = InvoiceService.getById(invoice.getId(), organizationId);
		if (existing == null)
			return notFound("Invoice");
		double amountPaid = existing.getAmountPaid() == null ? 0 : existing.getAmountPaid();
		String requestedStatus = invoice.getStatus();
		// Only payments change amount_paid; ownership fields are fixed.
		invoice.setAmountPaid(null);
		invoice.setOrganizationId(null);
		invoice.setPropertyId(null);
		invoice.setTenancyId(null);
		invoice.setUserId(null);
		invoice.setInvoiceNumber(null);
		ModelMerger.merge(existing, invoice);
		existing.setOrganizationId(organizationId);
		return respond(InvoiceService.updateInvoice(existing, amountPaid, requestedStatus));
	}
}
