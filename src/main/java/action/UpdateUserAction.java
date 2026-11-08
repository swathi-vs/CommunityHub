package action;

import model.Users;
import services.UserService;
import utils.ModelMerger;

public class UpdateUserAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long userId;
	private Users user = new Users();

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	@Override
	public void validate() {
		if (user.getGender() != null && !java.util.Arrays.asList("male", "female", "other", "prefer_not_to_say")
				.contains(user.getGender()))
			addFieldError("user.gender", "Gender must be male, female, other or prefer_not_to_say");
		if (!blank(user.getEmail()) && !user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
			addFieldError("user.email", "Invalid email");
		if (!blank(user.getPhone()) && !user.getPhone().matches("^[0-9]{10}$"))
			addFieldError("user.phone", "Phone must be 10 digits");
		if (!blank(user.getFullName()) && !user.getFullName().matches("^[A-Za-z ]{3,50}$"))
			addFieldError("user.fullName", "Invalid name");
	}

	@Override
	public String execute() {
		Users existing = UserService.getById(userId, organizationId);
		if (existing == null)
			return notFound("User");
		// Status, role and password are not editable here.
		user.setStatus(null);
		user.setRole(null);
		user.setPasswordHash(null);
		ModelMerger.merge(existing, user);
		existing.setId(userId);
		return respond(UserService.updateUser(existing));
	}
}
