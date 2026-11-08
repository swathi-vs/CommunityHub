package action;

import model.UserResponse;
import services.TenancyService;
import utils.TenantScoped;

public class GetTenancyUserAction extends BaseAction implements TenantScoped {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long tenancyId;
	private Long scopedUserId;

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setTenancyId(Long v) {
		this.tenancyId = v;
	}

	@Override
	public void setScopedUserId(Long v) {
		this.scopedUserId = v;
	}

	@Override
	public String execute() {
		Long userId = TenancyService.getUserIdByTenancy(tenancyId, organizationId);
		if (userId == null || (scopedUserId != null && !scopedUserId.equals(userId)))
			return notFound("Tenancy");
		UserResponse response = new UserResponse();
		response.setUserId(userId);
		return ok("Tenancy user", response);
	}
}
