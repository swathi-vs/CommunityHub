package interceptors;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * Runs after the params interceptor and re-applies the values taken from the URL
 * (e.g. organizationId={1}), so request parameters cannot override them.
 */
public class PathParamInterceptor extends AbstractInterceptor {

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		Map<String, String> params = invocation.getProxy().getConfig().getParams();
		if (params != null && !params.isEmpty()) {
			ValueStack stack = invocation.getStack();
			for (Map.Entry<String, String> e : params.entrySet()) {
				stack.setValue(e.getKey(), e.getValue());
			}
		}
		return invocation.invoke();
	}
}
