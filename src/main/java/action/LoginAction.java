package action;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.struts2.dispatcher.SessionMap;

import com.opensymphony.xwork2.ActionContext;

import model.Users;
import services.UserRoleService;
import services.UserService;

public class LoginAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private String email;
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void validate() {
		if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
			addFieldError("email", "Invalid email");
		if (blank(password))
			addFieldError("password", "Password is required");
	}

	@Override
	public String execute() {
		Users user = UserService.validateUsers(email, password);
		if (user == null)
			return unauthorized("Invalid email or password");
		if ("inactive".equalsIgnoreCase(user.getStatus()))
			return forbidden("Account is deactivated");

		Map<String, Object> session = ActionContext.getContext().getSession();
		if (session instanceof SessionMap)
			((SessionMap) session).invalidate();
		session = ActionContext.getContext().getSession();
		session.put("userId", user.getId());
		session.put("email", user.getEmail());
		session.put("fullName", user.getFullName());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("user", user);
		data.put("organizations", UserRoleService.getMemberships(user.getId()));
		return ok("Logged in", data);
	}
}
