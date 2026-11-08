package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.FieldRecordAssociation;
import utils.Log;

public class FieldRecordAssociationDAO {

	/** Column names follow <type>_col_<n>; anything else is rejected before it can reach SQL. */
	public static final String COLUMN_PATTERN = "^(str|int|dec|bool|date|time|datetime|text)_col_[0-9]{1,2}$";

	/** Active field of a form, with the physical column that stores its value. */
	public static class FieldSpec {
		public Long fieldDefinitionId;
		public String fieldKey;
		public String fieldLabel;
		public String fieldType;
		public boolean required;
		public String columnName;
	}

	public static boolean associate(Connection con, FieldRecordAssociation fra) throws SQLException {
		String query = "insert into field_record_association(form_id, field_definition_id, column_name, is_active) values (?,?,?,1)";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, fra.getFormId());
			ps.setLong(2, fra.getFieldDefinitionId());
			ps.setString(3, fra.getColumnName());
			return ps.executeUpdate() > 0;
		}
	}

	/** Columns currently in use by active fields of the form. */
	public static List<String> getUsedColumns(Connection con, Long formId, String prefix) throws SQLException {
		List<String> used = new ArrayList<>();
		String query = "select column_name from field_record_association where form_id = ? and is_active = 1 and column_name like ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, formId);
			ps.setString(2, prefix + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					used.add(rs.getString("column_name"));
			}
		}
		return used;
	}

	public static String getActiveColumn(Connection con, Long fieldDefinitionId) throws SQLException {
		String query = "select column_name from field_record_association where field_definition_id = ? and is_active = 1";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, fieldDefinitionId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString("column_name") : null;
			}
		}
	}

	public static void deactivateForField(Connection con, Long fieldDefinitionId) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(
				"update field_record_association set is_active = 0 where field_definition_id = ?")) {
			ps.setLong(1, fieldDefinitionId);
			ps.executeUpdate();
		}
	}

	public static void deactivateForForm(Connection con, Long formId) throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("update field_record_association set is_active = 0 where form_id = ?")) {
			ps.setLong(1, formId);
			ps.executeUpdate();
		}
	}

	public static List<FieldSpec> getActiveFields(Long formId) {
		String query = "select cfd.id, cfd.field_key, cfd.field_label, cfd.field_type, cfd.is_required, fra.column_name "
				+ "from field_record_association fra "
				+ "join custom_field_definitions cfd on cfd.id = fra.field_definition_id "
				+ "where fra.form_id = ? and fra.is_active = 1 and cfd.is_active = 1";
		List<FieldSpec> fields = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, formId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String column = rs.getString("column_name");
					if (column == null || !column.matches(COLUMN_PATTERN)) {
						Log.warn("Ignoring invalid column mapping: " + column);
						continue;
					}
					FieldSpec f = new FieldSpec();
					f.fieldDefinitionId = rs.getLong("id");
					f.fieldKey = rs.getString("field_key");
					f.fieldLabel = rs.getString("field_label");
					f.fieldType = rs.getString("field_type");
					f.required = rs.getBoolean("is_required");
					f.columnName = column;
					fields.add(f);
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return fields;
	}
}
