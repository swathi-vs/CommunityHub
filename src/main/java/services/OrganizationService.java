package services;

import dao.OrganizationDAO;
import dao.RolesDAO;
import dao.UserRoleDAO;
import model.Organization;
import model.ServiceResult;
import model.UserRole;
import utils.Db;
import utils.Log;
import utils.RequestContext;

public class OrganizationService {

	/** Creates the organization and makes the creator its admin, in one transaction. */
	public static ServiceResult createOrganization(Organization org, Long creatorUserId) {
		return Db.tx(con -> {
			Long adminRoleId = RolesDAO.findIdByName(con, RequestContext.ROLE_ADMIN);
			if (adminRoleId == null) {
				Log.warn("Role '" + RequestContext.ROLE_ADMIN + "' is missing. Run db/seed.sql.");
				return ServiceResult.error();
			}
			Long orgId = OrganizationDAO.insertOrganization(con, org);
			if (orgId == null)
				return ServiceResult.error();
			UserRole admin = new UserRole();
			admin.setOrganizationId(orgId);
			admin.setUserId(creatorUserId);
			admin.setRoleId(adminRoleId);
			admin.setAssignedBy(creatorUserId);
			UserRoleDAO.assignRole(con, admin);
			org.setId(orgId);
			org.setStatus("active");
			return ServiceResult.ok("Organization created", org);
		});
	}

	public static Organization getById(Long id) {
		return OrganizationDAO.getById(id);
	}

	public static boolean updateOrganization(Organization org) {
		return OrganizationDAO.updateOrganization(org);
	}
}
