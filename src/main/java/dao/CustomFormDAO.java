package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import model.CustomForm;
import utils.Db;
import utils.Log;
import utils.QueryExecutor;
import utils.QueryFilter;

public class CustomFormDAO {

	private static CustomForm map(ResultSet rs) throws SQLException {
		CustomForm f = new CustomForm();
		f.setId(rs.getLong("id"));
		f.setOrganizationId(rs.getLong("organization_id"));
		f.setFormName(rs.getString("form_name"));
		f.setDescription(rs.getString("description"));
		f.setIsActive(rs.getBoolean("is_active"));
		f.setCreatedAt(rs.getTimestamp("created_at"));
		return f;
	}

	public static Long createForm(CustomForm form) {
		String query = "insert into custom_forms(organization_id, form_name, description, is_active) values (?,?,?,1)";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, form.getOrganizationId());
			ps.setString(2, form.getFormName());
			ps.setString(3, form.getDescription());
			ps.executeUpdate();
			return Db.generatedId(ps);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean deactivateForm(Connection con, Long id, Long organizationId) throws SQLException {
		String sql = "UPDATE custom_forms SET is_active = 0 WHERE id = ? AND organization_id = ? AND is_active = 1";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, organizationId);
			return ps.executeUpdate() > 0;
		}
	}

	public static List<CustomForm> getForms(Long orgId, QueryFilter filter) {
		return QueryExecutor.getList("cf.organization_id = ?", orgId, filter, CustomFormDAO::map);
	}

	public static CustomForm getById(Long id, Long orgId) {
		String sql = "SELECT * FROM custom_forms WHERE id = ? AND organization_id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, orgId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return map(rs);
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}
}
