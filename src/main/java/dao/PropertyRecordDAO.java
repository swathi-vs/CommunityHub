package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import model.ColumnValue;
import model.FieldValue;

public class PropertyRecordDAO {

	/**
	 * Inserts or updates the property's record for a form (needs a unique key on
	 * property_id, form_id). Columns must come from RecordService validation.
	 */
	public static boolean save(Connection con, Long propertyId, Long orgId, Long formId, List<ColumnValue> values)
			throws SQLException {
		StringBuilder cols = new StringBuilder("property_id, organization_id, form_id");
		StringBuilder vals = new StringBuilder("?, ?, ?");
		StringBuilder update = new StringBuilder("organization_id = VALUES(organization_id), form_id = VALUES(form_id)");
		for (ColumnValue v : values) {
			String col = v.getColumn();
			if (!col.matches(FieldRecordAssociationDAO.COLUMN_PATTERN))
				throw new IllegalArgumentException("Invalid column: " + col);
			cols.append(", ").append(col);
			vals.append(", ?");
			update.append(", ").append(col).append(" = VALUES(").append(col).append(")");
		}
		String sql = "INSERT INTO property_record(" + cols + ") VALUES(" + vals + ") ON DUPLICATE KEY UPDATE " + update;
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, propertyId);
			ps.setLong(2, orgId);
			ps.setLong(3, formId);
			int i = 4;
			for (ColumnValue v : values)
				ps.setObject(i++, v.getValue());
			return ps.executeUpdate() > 0;
		}
	}

	/** Field values of the property's record for its currently active form. */
	public static List<FieldValue> getByPropertyId(Long propertyId) {
		String sql = "SELECT cfd.field_label, cfd.field_key, cfd.field_type, cfd.id as field_def_id, cfd.is_required, "
				+ "cfd.default_value, cfd.display_order, fra.column_name, pr.* FROM property_record pr "
				+ "JOIN form_relationships frel ON frel.property_id = pr.property_id AND frel.form_id = pr.form_id "
				+ "AND frel.is_active = 1 "
				+ "JOIN field_record_association fra ON fra.form_id = pr.form_id AND fra.is_active = 1 "
				+ "JOIN custom_field_definitions cfd ON cfd.id = fra.field_definition_id AND cfd.is_active = 1 "
				+ "WHERE pr.property_id = ? ORDER BY cfd.display_order";
		return RecordReader.read(sql, propertyId);
	}

	public static void clearColumn(Connection con, Long formId, String column) throws SQLException {
		if (!column.matches(FieldRecordAssociationDAO.COLUMN_PATTERN))
			throw new IllegalArgumentException("Invalid column: " + column);
		try (PreparedStatement ps = con
				.prepareStatement("UPDATE property_record SET " + column + " = NULL WHERE form_id = ?")) {
			ps.setLong(1, formId);
			ps.executeUpdate();
		}
	}
}
