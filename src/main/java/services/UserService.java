package services;

import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import dao.TenancyDAO;
import dao.UserDAO;
import dao.UserRoleDAO;
import model.ServiceResult;
import model.Users;
import utils.Db;
import utils.QueryFilter;

public class UserService {

	/** Returns the user when the email/password match, otherwise null. */
	public static Users validateUsers(String email, String password) {
		Users user = UserDAO.login(email);
		if (user == null || user.getPasswordHash() == null)
			return null;
		boolean matched;
		try {
			matched = BCrypt.checkpw(password, user.getPasswordHash());
		} catch (IllegalArgumentException e) {
			matched = false;
		}
		return matched ? user : null;
	}

	public static ServiceResult registerUser(Users user, String password) {
		user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
		user.setRole(utils.RequestContext.ROLE_TENANT);
		Long id = UserDAO.insertUser(user);
		if (id == null)
			return ServiceResult.error();
		user.setId(id);
		return ServiceResult.ok("Account created", user);
	}

	public static Users getById(Long id, Long organizationId) {
		return UserDAO.getById(id, organizationId);
	}

	public static List<String> getCapabilities(Long userId, Long organizationId) {
		return UserDAO.getCapabilities(userId, organizationId);
	}

	/**
	 * Removes the user from one organization (deletes their roles there). The login itself is
	 * only deactivated when the user no longer belongs to any organization.
	 */
	public static ServiceResult deactivateUser(Long id, Long organizationId, Long actingUserId) {
		if (id.equals(actingUserId))
			return ServiceResult.badRequest("You cannot deactivate yourself");
		if (UserDAO.getById(id, organizationId) == null)
			return ServiceResult.notFound("User");
		return Db.tx(con -> {
			if (TenancyDAO.hasActiveForUser(con, id, organizationId))
				return ServiceResult.conflict("User has an active tenancy. Terminate it first.");
			UserRoleDAO.removeFromOrganization(con, id, organizationId);
			if (!UserRoleDAO.hasAnyRole(con, id))
				UserDAO.setStatus(con, id, "inactive");
			return ServiceResult.ok("User removed from organization");
		});
	}

	public static List<Users> getUsers(Long orgId, QueryFilter filter) {
		return UserDAO.getUsers(orgId, filter);
	}

	public static ServiceResult changePassword(Long userId, String currentPassword, String newPassword) {
		Users user = UserDAO.getPasswordHashById(userId);
		if (user == null)
			return ServiceResult.notFound("User");
		if (!BCrypt.checkpw(currentPassword, user.getPasswordHash()))
			return ServiceResult.badRequest("Current password is incorrect");
		String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
		return UserDAO.updatePassword(userId, newHash) ? ServiceResult.ok("Password changed") : ServiceResult.error();
	}

	public static boolean emailExists(String email) {
		return UserDAO.findIdByEmail(email) != null;
	}

	public static boolean phoneExists(String phone) {
		return UserDAO.findIdByPhone(phone) != null;
	}

	public static ServiceResult updateUser(Users user) {
		if (user.getEmail() != null) {
			Long found = UserDAO.findIdByEmail(user.getEmail());
			if (found != null && !found.equals(user.getId()))
				return ServiceResult.conflict("Email is already registered");
		}
		if (user.getPhone() != null) {
			Long found = UserDAO.findIdByPhone(user.getPhone());
			if (found != null && !found.equals(user.getId()))
				return ServiceResult.conflict("Phone number is already registered");
		}
		return UserDAO.updateUser(user) ? ServiceResult.ok("User updated", user) : ServiceResult.error();
	}
}
