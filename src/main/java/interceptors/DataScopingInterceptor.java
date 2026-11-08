package interceptors;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import utils.RequestContext;
import utils.TenantScoped;

/**
 * Limits TenantScoped actions to the current user's own data when the user is only a
 * tenant in the organization. Must run after the params interceptor so the value set
 * here always wins over anything in the request.
 */
public class DataScopingInterceptor extends AbstractInterceptor {

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		Object action = invocation.getAction();
		if (action instanceof TenantScoped) {
			Map<String, Object> session = invocation.getInvocationContext().getSession();
			Long userId = (Long) session.get("userId");
			boolean tenantOnly = RequestContext.isTenantOnly(RequestContext.roles());
			((TenantScoped) action).setScopedUserId(tenantOnly ? userId : null);
		}
		return invocation.invoke();
	}
}
