package action;

import model.Users;
import services.UserService;

public class SignUpAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Users user = new Users();
	private String password;

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void validate() {
		if (user.getGender() != null && !java.util.Arrays.asList("male", "female", "other", "prefer_not_to_say")
				.contains(user.getGender()))
			addFieldError("user.gender", "Gender must be male, female, other or prefer_not_to_say");
		if (user.getFullName() == null || !user.getFullName().matches("^[A-Za-z ]{3,50}$"))
			addFieldError("user.fullName", "Invalid name");
		if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
			addFieldError("user.email", "Invalid email");
		else if (UserService.emailExists(user.getEmail()))
			addFieldError("user.email", "Email is already registered");
		if (user.getPhone() == null || !user.getPhone().matches("^[0-9]{10}$"))
			addFieldError("user.phone", "Invalid phone");
		else if (UserService.phoneExists(user.getPhone()))
			addFieldError("user.phone", "Phone number is already registered");
		if (password == null || password.length() < 6)
			addFieldError("password", "Password must be at least 6 characters");
	}

	@Override
	public String execute() {
		return respond(UserService.registerUser(user, password));
	}
}
