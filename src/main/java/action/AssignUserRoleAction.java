package action;

import model.UserRole;
import services.UserRoleService;

public class AssignUserRoleAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private UserRole userRole = new UserRole();

	public UserRole getUserRole() {
		return userRole;
	}

	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	@Override
	public void validate() {
		if (userRole.getUserId() == null)
			addFieldError("userRole.userId", "User required");
		if (userRole.getRoleId() == null)
			addFieldError("userRole.roleId", "Role required");
	}

	@Override
	public String execute() {
		userRole.setAssignedBy(currentUserId());
		return respond(UserRoleService.assignRole(userRole));
	}
}
