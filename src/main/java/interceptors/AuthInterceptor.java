package interceptors;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import model.ApiResponse;
import utils.Json;

public class AuthInterceptor extends AbstractInterceptor {

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		Map<String, Object> session = invocation.getInvocationContext().getSession();
		Object userId = session == null ? null : session.get("userId");
		if (userId == null) {
			Json.write(ApiResponse.unauthorized("Unauthorized"));
			return null;
		}
		return invocation.invoke();
	}
}
