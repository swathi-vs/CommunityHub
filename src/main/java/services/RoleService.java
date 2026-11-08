package services;

import java.util.List;

import dao.RolesDAO;
import model.Roles;

public class RoleService {

	public static List<Roles> getRoles() {

		return RolesDAO.getRoles();
	}
}