package action;

import services.UserService;

public class DeactivateUserAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long userId;
	private Long organizationId;

	public void setUserId(Long v) {
		this.userId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	@Override
	public void validate() {
		if (userId == null)
			addFieldError("userId", "User required");
	}

	@Override
	public String execute() {
		return respond(UserService.deactivateUser(userId, organizationId, currentUserId()));
	}
}
