package action;

import java.util.Map;

import org.apache.struts2.dispatcher.SessionMap;

import com.opensymphony.xwork2.ActionContext;

public class LogoutAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	@Override
	public String execute() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		if (session instanceof SessionMap)
			((SessionMap) session).invalidate();
		else if (session != null)
			session.clear();
		return ok("Logged out");
	}
}
