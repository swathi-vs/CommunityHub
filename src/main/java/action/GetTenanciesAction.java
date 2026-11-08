package action;

import model.Tenancy;
import services.TenancyService;
import utils.QueryFilter;
import utils.TenantScoped;

public class GetTenanciesAction extends BaseAction implements TenantScoped {

	private static final long serialVersionUID = 1L;

	private Long scopedUserId;
	private Long organizationId;
	private Long tenancyId;
	private String status;
	private Long propertyId;
	private String tenantName;
	private String roomNumber;
	private int page = 1, size = 20;
	private String orderBy, orderDir;

	@Override
	public void setScopedUserId(Long v) {
		this.scopedUserId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setTenancyId(Long v) {
		this.tenancyId = v;
	}

	public void setStatus(String v) {
		this.status = v;
	}

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setTenantName(String v) {
		this.tenantName = v;
	}

	public void setRoomNumber(String v) {
		this.roomNumber = v;
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
		if (tenancyId != null) {
			Tenancy tenancy = TenancyService.getById(tenancyId, organizationId);
			if (tenancy == null || (scopedUserId != null && !scopedUserId.equals(tenancy.getUserId())))
				return notFound("Tenancy");
			return ok("Tenancy", tenancy);
		}
		QueryFilter filter = new QueryFilter("tenancies").add("status", status).add("property_id", propertyId)
				.add("tenant_name", tenantName).add("room_number", roomNumber).add("user_id", scopedUserId)
				.orderBy(orderBy, orderDir).page(page, size);
		return ok("Tenancies", TenancyService.getTenancies(organizationId, filter));
	}
}
