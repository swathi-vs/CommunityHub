package action;

import services.PaymentService;
import utils.QueryFilter;
import utils.TenantScoped;

public class GetPaymentsAction extends BaseAction implements TenantScoped {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private String method;
	private Long invoiceId;
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

	public void setMethod(String v) {
		this.method = v;
	}

	public void setInvoiceId(Long v) {
		this.invoiceId = v;
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
		QueryFilter filter = new QueryFilter("payments").add("method", method).add("invoice_id", invoiceId)
				.add("user_id", scopedUserId).orderBy(orderBy, orderDir).page(page, size);
		return ok("Payments", PaymentService.getPayments(organizationId, filter));
	}
}
