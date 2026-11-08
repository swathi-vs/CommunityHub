package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.CustomFieldDefinition;
import utils.Db;
import utils.Log;

public class CustomFieldDefinitionDAO {

	private static CustomFieldDefinition map(ResultSet rs) throws SQLException {
		CustomFieldDefinition field = new CustomFieldDefinition();
		field.setId(rs.getLong("id"));
		field.setFormId(rs.getLong("form_id"));
		field.setOrganizationId(rs.getLong("organization_id"));
		field.setFieldKey(rs.getString("field_key"));
		field.setFieldLabel(rs.getString("field_label"));
		field.setFieldType(rs.getString("field_type"));
		field.setIsRequired(rs.getBoolean("is_required"));
		field.setDefaultValue(rs.getString("default_value"));
		field.setDisplayOrder(rs.getInt("display_order"));
		field.setHelpText(rs.getString("help_text"));
		field.setIsActive(rs.getBoolean("is_active"));
		return field;
	}

	public static Long createFieldDefinition(Connection con, CustomFieldDefinition field) throws SQLException {
		String query = "insert into custom_field_definitions(organization_id, form_id, field_key, field_label, field_type, "
				+ "is_required, default_value, display_order, help_text, is_active) values (?,?,?,?,?,?,?,?,?,1)";
		try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, field.getOrganizationId());
			ps.setLong(2, field.getFormId());
			ps.setString(3, field.getFieldKey());
			ps.setString(4, field.getFieldLabel());
			ps.setString(5, field.getFieldType());
			ps.setBoolean(6, Boolean.TRUE.equals(field.getIsRequired()));
			ps.setString(7, field.getDefaultValue());
			ps.setInt(8, field.getDisplayOrder() == null ? 0 : field.getDisplayOrder());
			ps.setString(9, field.getHelpText());
			ps.executeUpdate();
			return Db.generatedId(ps);
		}
	}

	public static CustomFieldDefinition getById(Long id, Long formId, Long organizationId) {
		String sql = "select * from custom_field_definitions where id = ? and form_id = ? and organization_id = ? and is_active = 1";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, formId);
			ps.setLong(3, organizationId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return map(rs);
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean fieldKeyExists(Connection con, Long formId, String fieldKey) throws SQLException {
		String sql = "select 1 from custom_field_definitions where form_id = ? and field_key = ? and is_active = 1";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, formId);
			ps.setString(2, fieldKey);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	public static boolean deactivate(Connection con, Long id, Long organizationId) throws SQLException {
		String sql = "update custom_field_definitions set is_active = 0 where id = ? and organization_id = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, organizationId);
			return ps.executeUpdate() > 0;
		}
	}

	public static void deactivateForForm(Connection con, Long formId) throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("update custom_field_definitions set is_active = 0 where form_id = ?")) {
			ps.setLong(1, formId);
			ps.executeUpdate();
		}
	}

	public static List<CustomFieldDefinition> getByFormId(Long formId) {
		String sql = "SELECT cfd.*, fra.column_name FROM custom_field_definitions cfd "
				+ "JOIN field_record_association fra ON fra.field_definition_id = cfd.id AND fra.is_active = 1 "
				+ "WHERE cfd.form_id = ? AND cfd.is_active = 1 ORDER BY cfd.display_order";
		List<CustomFieldDefinition> fields = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, formId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CustomFieldDefinition field = map(rs);
					field.setColumnName(rs.getString("column_name"));
					fields.add(field);
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return fields;
	}
}
