package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.UserRole;
import utils.Db;
import utils.Log;

public class UserRoleDAO {

	public static boolean assignRole(Connection con, UserRole userRole) throws SQLException {
		String query = "insert into user_roles(organization_id, user_id, role_id, property_id, assigned_by) values(?,?,?,?,?)";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, userRole.getOrganizationId());
			ps.setLong(2, userRole.getUserId());
			ps.setLong(3, userRole.getRoleId());
			Db.setLong(ps, 4, userRole.getPropertyId());
			Db.setLong(ps, 5, userRole.getAssignedBy());
			return ps.executeUpdate() > 0;
		}
	}

	public static boolean assignRole(UserRole userRole) {
		try (Connection con = DBConnection.getConnection()) {
			return assignRole(con, userRole);
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}

	public static boolean roleAssignmentExists(UserRole ur) {
		String query = "select 1 from user_roles where organization_id = ? and user_id = ? and role_id = ? "
				+ "and (property_id = ? or (property_id is null and ? is null))";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, ur.getOrganizationId());
			ps.setLong(2, ur.getUserId());
			ps.setLong(3, ur.getRoleId());
			Db.setLong(ps, 4, ur.getPropertyId());
			Db.setLong(ps, 5, ur.getPropertyId());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}

	/** Role names the user holds in an organization (empty when not a member). */
	public static List<String> getRoleNames(Long userId, Long organizationId) {
		String query = "select distinct r.name from user_roles ur join roles r on r.id = ur.role_id "
				+ "where ur.user_id = ? and ur.organization_id = ?";
		List<String> roles = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, userId);
			ps.setLong(2, organizationId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					roles.add(rs.getString("name"));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return roles;
	}

	/** Organizations the user belongs to, with the role names held in each. */
	public static List<Map<String, Object>> getMemberships(Long userId) {
		String query = "select ur.organization_id, o.name as organization_name, r.name as role_name, ur.property_id "
				+ "from user_roles ur join roles r on r.id = ur.role_id "
				+ "join organizations o on o.id = ur.organization_id "
				+ "where ur.user_id = ? order by ur.organization_id";
		Map<Long, Map<String, Object>> byOrg = new LinkedHashMap<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Long orgId = rs.getLong("organization_id");
					Map<String, Object> m = byOrg.computeIfAbsent(orgId, k -> {
						Map<String, Object> n = new LinkedHashMap<>();
						n.put("organizationId", k);
						n.put("roles", new ArrayList<Map<String, Object>>());
						return n;
					});
					m.put("organizationName", rs.getString("organization_name"));
					Map<String, Object> role = new LinkedHashMap<>();
					role.put("role", rs.getString("role_name"));
					role.put("propertyId", Db.getLong(rs, "property_id"));
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> roles = (List<Map<String, Object>>) m.get("roles");
					roles.add(role);
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return new ArrayList<>(byOrg.values());
	}

	public static int removeFromOrganization(Connection con, Long userId, Long organizationId) throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("delete from user_roles where user_id = ? and organization_id = ?")) {
			ps.setLong(1, userId);
			ps.setLong(2, organizationId);
			return ps.executeUpdate();
		}
	}

	public static boolean hasAnyRole(Connection con, Long userId) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement("select 1 from user_roles where user_id = ? limit 1")) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}
}
