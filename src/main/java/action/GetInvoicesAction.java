package action;

import model.Invoice;
import services.InvoiceService;
import utils.QueryFilter;
import utils.TenantScoped;

public class GetInvoicesAction extends BaseAction implements TenantScoped {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long invoiceId;
	private String status;
	private String invoiceType;
	private Long tenancyId;
	private Long propertyId;
	private int page = 1, size = 20;
	private String orderBy, orderDir;
	private Long scopedUserId;

	@Override
	public void setScopedUserId(Long v) {
		this.scopedUserId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setInvoiceId(Long v) {
		this.invoiceId = v;
	}

	public void setStatus(String v) {
		this.status = v;
	}

	public void setInvoiceType(String v) {
		this.invoiceType = v;
	}

	public void setTenancyId(Long v) {
		this.tenancyId = v;
	}

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setPage(int v) {
		this.page = v;
	}

	public void setSize(int v) {
		this.size = v;
	}

	public void setOrderBy(String v) {
		this.orderBy = v;
	}

	public void setOrderDir(String v) {
		this.orderDir = v;
	}

	@Override
	public String execute() {
		if (invoiceId != null) {
			Invoice invoice = InvoiceService.getById(invoiceId, organizationId);
			if (invoice == null || (scopedUserId != null && !scopedUserId.equals(invoice.getUserId())))
				return notFound("Invoice");
			return ok("Invoice", invoice);
		}
		QueryFilter filter = new QueryFilter("invoices").add("status", status).add("invoice_type", invoiceType)
				.add("tenancy_id", tenancyId).add("property_id", propertyId).add("user_id", scopedUserId)
				.orderBy(orderBy, orderDir).page(page, size);
		return ok("Invoices", InvoiceService.getInvoices(organizationId, filter));
	}
}
