package action;

import model.Users;
import services.UserService;
import utils.QueryFilter;

public class UsersAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long userId;
	private String role;
	private Long roleId;
	private Long propertyId;
	private String status;
	private int page = 1, size = 20;
	private String orderBy, orderDir;

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setUserId(Long v) {
		this.userId = v;
	}

	public void setRole(String v) {
		this.role = v;
	}

	public void setRoleId(Long v) {
		this.roleId = v;
	}

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setStatus(String v) {
		this.status = v;
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
		if (userId != null) {
			Users user = UserService.getById(userId, organizationId);
			return user == null ? notFound("User") : ok("User", user);
		}
		QueryFilter filter = new QueryFilter("users").add("role", role).add("role_id", roleId).add("status", status)
				.add("property_id", propertyId).orderBy(orderBy, orderDir).page(page, size);
		return ok("Users", UserService.getUsers(organizationId, filter));
	}
}
