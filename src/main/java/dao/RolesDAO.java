package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Roles;
import utils.Log;

public class RolesDAO {

	public static List<Roles> getRoles() {
		List<Roles> roles = new ArrayList<>();
		String query = "SELECT * FROM roles ORDER BY id";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Roles role = new Roles();
				role.setId(rs.getLong("id"));
				role.setName(rs.getString("name"));
				role.setDescription(rs.getString("description"));
				roles.add(role);
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return roles;
	}

	public static boolean exists(Long id) {
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement("select 1 from roles where id = ?")) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}

	public static Long findIdByName(Connection con, String name) throws java.sql.SQLException {
		try (PreparedStatement ps = con.prepareStatement("select id from roles where name = ? and organization_id is null and is_active = 1 limit 1")) {
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getLong("id") : null;
			}
		}
	}
}
