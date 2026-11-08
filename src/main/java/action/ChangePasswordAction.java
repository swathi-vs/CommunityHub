package action;

import services.UserService;

public class ChangePasswordAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private String currentPassword;
	private String newPassword;
	private String confirmPassword;

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	@Override
	public void validate() {
		if (blank(currentPassword))
			addFieldError("currentPassword", "Current password is required");
		if (newPassword == null || newPassword.length() < 6)
			addFieldError("newPassword", "New password must be at least 6 characters");
		else if (!newPassword.equals(confirmPassword))
			addFieldError("confirmPassword", "Passwords do not match");
	}

	@Override
	public String execute() {
		return respond(UserService.changePassword(currentUserId(), currentPassword, newPassword));
	}
}
