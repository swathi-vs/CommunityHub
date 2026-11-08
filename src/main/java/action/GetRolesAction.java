package action;

import services.RoleService;

public class GetRolesAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	@Override
	public String execute() {
		return ok("Roles", RoleService.getRoles());
	}
}
