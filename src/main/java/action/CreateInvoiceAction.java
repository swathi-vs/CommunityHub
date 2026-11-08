package action;

import model.Invoice;
import services.InvoiceService;

public class CreateInvoiceAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Invoice invoice = new Invoice();

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	@Override
	public void validate() {
		if (invoice.getTenancyId() == null)
			addFieldError("invoice.tenancyId", "Tenancy required");
		if (!InvoiceService.TYPES.contains(invoice.getInvoiceType()))
			addFieldError("invoice.invoiceType", "Invoice type must be one of " + InvoiceService.TYPES);
		if (invoice.getTotalAmount() == null || invoice.getTotalAmount() <= 0)
			addFieldError("invoice.totalAmount", "Valid amount required");
		if (invoice.getDueDate() == null)
			addFieldError("invoice.dueDate", "Due date required");
	}

	@Override
	public String execute() {
		invoice.setCreatedBy(currentUserId());
		return respond(InvoiceService.createInvoice(invoice));
	}
}
