package utils;

/**
 * Actions whose results must be limited to the current user when that user is only a
 * tenant in the organization. DataScopingInterceptor sets the value after request params,
 * so a client cannot supply it.
 */
public interface TenantScoped {
	void setScopedUserId(Long userId);
}
