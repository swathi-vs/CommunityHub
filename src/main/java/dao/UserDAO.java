package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Users;
import utils.Log;
import utils.QueryExecutor;
import utils.QueryFilter;
import utils.UpdateBuilder;

public class UserDAO {

	private static Users map(ResultSet rs) throws SQLException {
		Users u = new Users();
		u.setId(rs.getLong("id"));
		u.setFullName(rs.getString("full_name"));
		u.setEmail(rs.getString("email"));
		u.setPhone(rs.getString("phone"));
		u.setRole(rs.getString("role"));
		u.setDob(rs.getDate("date_of_birth"));
		u.setGender(rs.getString("gender"));
		u.setNationality(rs.getString("nationality"));
		u.setStatus(rs.getString("status"));
		return u;
	}

	public static Users login(String email) {
		String query = "select * from users where email = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Users user = map(rs);
					user.setPasswordHash(rs.getString("password_hash"));
					return user;
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static Long insertUser(Users user) {
		String query = "insert into users(full_name,email,phone,password_hash,date_of_birth,gender,nationality,role,status) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, 'active')";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, user.getFullName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getPhone());
			ps.setString(4, user.getPasswordHash());
			ps.setDate(5, user.getDob());
			ps.setString(6, user.getGender());
			ps.setString(7, user.getNationality());
			ps.setString(8, user.getRole());
			ps.executeUpdate();
			return utils.Db.generatedId(ps);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	/** Capability codes the user holds through their roles in one organization. */
	public static List<String> getCapabilities(Long userId, Long organizationId) {
		String query = "select distinct c.code from capabilities c "
				+ "join role_capabilities rc on rc.capability_id = c.id "
				+ "join user_roles ur on ur.role_id = rc.role_id "
				+ "where ur.user_id = ? and ur.organization_id = ?";
		List<String> capabilities = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, userId);
			ps.setLong(2, organizationId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					capabilities.add(rs.getString("code"));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return capabilities;
	}

	/** A user who is a member (holds any role) of the organization. */
	public static Users getById(Long id, Long organizationId) {
		String query = "select u.* from users u where u.id = ? and exists "
				+ "(select 1 from user_roles ur where ur.user_id = u.id and ur.organization_id = ?)";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, id);
			ps.setLong(2, organizationId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return map(rs);
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean exists(Long id) {
		String query = "select 1 from users where id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}

	public static boolean setStatus(Connection con, Long id, String status) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement("update users set status = ? where id = ?")) {
			ps.setString(1, status);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	public static List<Users> getUsers(Long orgId, QueryFilter filter) {
		return QueryExecutor.getList("ur.organization_id = ?", orgId, filter, UserDAO::map);
	}

	public static Users getPasswordHashById(Long id) {
		String query = "SELECT id, password_hash FROM users WHERE id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Users u = new Users();
					u.setId(rs.getLong("id"));
					u.setPasswordHash(rs.getString("password_hash"));
					return u;
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean updatePassword(Long id, String newHash) {
		String query = "UPDATE users SET password_hash = ? WHERE id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, newHash);
			ps.setLong(2, id);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}

	private static Long findIdBy(String column, String value) {
		String query = "SELECT id FROM users WHERE " + column + " = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, value);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getLong("id");
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static Long findIdByEmail(String email) {
		return findIdBy("email", email);
	}

	public static Long findIdByPhone(String phone) {
		return findIdBy("phone", phone);
	}

	/** Updates profile fields only; status is managed through deactivation. */
	public static boolean updateUser(Users u) {
		return new UpdateBuilder("users")
				.set("full_name", u.getFullName())
				.set("email", u.getEmail())
				.set("phone", u.getPhone())
				.set("date_of_birth", u.getDob())
				.set("gender", u.getGender())
				.set("nationality", u.getNationality())
				.where("id = ?", u.getId())
				.execute();
	}
}
