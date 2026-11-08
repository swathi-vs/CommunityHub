package services;

import java.util.List;
import java.util.Map;

import dao.PropertyDAO;
import dao.RolesDAO;
import dao.UserDAO;
import dao.UserRoleDAO;
import model.ServiceResult;
import model.UserRole;

public class UserRoleService {

	public static ServiceResult assignRole(UserRole userRole) {
		if (!UserDAO.exists(userRole.getUserId()))
			return ServiceResult.notFound("User");
		if (!RolesDAO.exists(userRole.getRoleId()))
			return ServiceResult.notFound("Role");
		if (userRole.getPropertyId() != null
				&& PropertyDAO.getById(userRole.getPropertyId(), userRole.getOrganizationId()) == null)
			return ServiceResult.notFound("Property");
		if (UserRoleDAO.roleAssignmentExists(userRole))
			return ServiceResult.conflict("User already has this role");
		return UserRoleDAO.assignRole(userRole) ? ServiceResult.ok("Role assigned") : ServiceResult.error();
	}

	public static List<String> getRoleNames(Long userId, Long organizationId) {
		return UserRoleDAO.getRoleNames(userId, organizationId);
	}

	public static List<Map<String, Object>> getMemberships(Long userId) {
		return UserRoleDAO.getMemberships(userId);
	}
}
