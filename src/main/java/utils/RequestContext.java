package utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/** Per-request values set by the interceptors (organization access, capabilities, roles). */
public final class RequestContext {

	public static final String ORG_ID = "communityhub.orgId";
	public static final String ORG_CAPABILITIES = "communityhub.orgCapabilities";
	public static final String ORG_ROLES = "communityhub.orgRoles";

	public static final String ROLE_TENANT = "tenant";
	public static final String ROLE_ADMIN = "admin";

	private RequestContext() {
	}

	public static Long currentUserId() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		return session == null ? null : (Long) session.get("userId");
	}

	@SuppressWarnings("unchecked")
	public static List<String> capabilities() {
		Object caps = ActionContext.getContext().get(ORG_CAPABILITIES);
		return caps == null ? Collections.emptyList() : (List<String>) caps;
	}

	@SuppressWarnings("unchecked")
	public static List<String> roles() {
		Object roles = ActionContext.getContext().get(ORG_ROLES);
		return roles == null ? Collections.emptyList() : (List<String>) roles;
	}

	public static boolean hasCapability(String code) {
		return capabilities().contains(code);
	}

	/** A user is tenant-scoped when every role they hold in the organization is "tenant". */
	public static boolean isTenantOnly(List<String> roles) {
		return !roles.isEmpty() && roles.stream().allMatch(ROLE_TENANT::equalsIgnoreCase);
	}

	/**
	 * Reads the organization id bound from the URL (an action param named organizationId
	 * or ending in .organizationId). Returns null when the route has none, -1 when invalid.
	 */
	public static Long routeOrganizationId(ActionInvocation invocation) {
		Map<String, String> params = invocation.getProxy().getConfig().getParams();
		if (params == null)
			return null;
		for (Map.Entry<String, String> e : params.entrySet()) {
			String key = e.getKey();
			if (key.equals("organizationId") || key.endsWith(".organizationId")) {
				try {
					return Long.valueOf(e.getValue());
				} catch (NumberFormatException ex) {
					return -1L;
				}
			}
		}
		return null;
	}
}
