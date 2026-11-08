package interceptors;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import model.ApiResponse;
import services.UserRoleService;
import services.UserService;
import utils.Json;
import utils.RequestContext;

/**
 * Checks that the user belongs to the organization in the URL and holds the required
 * capabilities in that organization. Capabilities are always loaded per organization,
 * so a role in one organization grants nothing in another.
 */
public class CapabilityInterceptor extends AbstractInterceptor {

	private String requiredCapability;
	private boolean requireAll = true;

	public void setRequiredCapability(String cap) {
		this.requiredCapability = cap;
	}

	public void setRequireAll(boolean requireAll) {
		this.requireAll = requireAll;
	}

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionContext ctx = invocation.getInvocationContext();
		Map<String, Object> session = ctx.getSession();
		Long userId = (Long) session.get("userId");

		Long orgId = RequestContext.routeOrganizationId(invocation);
		List<String> capabilities = Collections.emptyList();
		if (orgId != null) {
			if (orgId <= 0)
				return deny(400, "Invalid organization id");
			List<String> roles = UserRoleService.getRoleNames(userId, orgId);
			if (roles.isEmpty())
				return deny(403, "Forbidden - you are not a member of this organization");
			capabilities = UserService.getCapabilities(userId, orgId);
			ctx.put(RequestContext.ORG_ID, orgId);
			ctx.put(RequestContext.ORG_ROLES, roles);
			ctx.put(RequestContext.ORG_CAPABILITIES, capabilities);
		}

		if (requiredCapability == null || requiredCapability.trim().isEmpty())
			return invocation.invoke();

		String[] required = requiredCapability.split(",");
		if (requireAll) {
			for (String cap : required) {
				if (!capabilities.contains(cap.trim()))
					return deny(403, "Forbidden - missing capability: " + cap.trim());
			}
		} else {
			boolean hasAny = false;
			for (String cap : required) {
				if (capabilities.contains(cap.trim())) {
					hasAny = true;
					break;
				}
			}
			if (!hasAny)
				return deny(403, "Forbidden - missing capability: " + requiredCapability);
		}
		return invocation.invoke();
	}

	private String deny(int status, String message) {
		Json.write(ApiResponse.of(false, message, status, null));
		return null;
	}
}
