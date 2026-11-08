package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.ColumnValue;
import model.FeatureRecordSummary;
import model.FieldValue;
import utils.Db;
import utils.Log;

public class FeatureRecordDAO {

	/** Saves a record. Columns must come from RecordService validation, never from the request. */
	public static Long save(Long formId, Long orgId, Long tenancyId, List<ColumnValue> values) {
		StringBuilder cols = new StringBuilder("form_id, organization_id, tenancy_id");
		StringBuilder vals = new StringBuilder("?, ?, ?");
		for (ColumnValue v : values) {
			if (!v.getColumn().matches(FieldRecordAssociationDAO.COLUMN_PATTERN))
				throw new IllegalArgumentException("Invalid column: " + v.getColumn());
			cols.append(", ").append(v.getColumn());
			vals.append(", ?");
		}
		String sql = "INSERT INTO feature_record(" + cols + ") VALUES(" + vals + ")";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, formId);
			ps.setLong(2, orgId);
			ps.setLong(3, tenancyId);
			int i = 4;
			for (ColumnValue v : values)
				ps.setObject(i++, v.getValue());
			ps.executeUpdate();
			return Db.generatedId(ps);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	private static FeatureRecordSummary mapSummary(ResultSet rs) throws SQLException {
		FeatureRecordSummary rec = new FeatureRecordSummary();
		rec.setId(rs.getLong("id"));
		rec.setFormId(rs.getLong("form_id"));
		rec.setOrganizationId(rs.getLong("organization_id"));
		rec.setTenancyId(rs.getLong("tenancy_id"));
		rec.setCreatedAt(rs.getTimestamp("created_at"));
		return rec;
	}

	public static List<FeatureRecordSummary> getByFormId(Long formId, Long orgId) {
		String sql = "SELECT id, form_id, organization_id, tenancy_id, created_at FROM feature_record "
				+ "WHERE form_id = ? AND organization_id = ? ORDER BY created_at DESC";
		List<FeatureRecordSummary> records = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, formId);
			ps.setLong(2, orgId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					records.add(mapSummary(rs));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return records;
	}

	public static List<FeatureRecordSummary> getByFormAndUser(Long formId, Long organizationId, Long userId) {
		String sql = "SELECT fr.id, fr.form_id, fr.organization_id, fr.tenancy_id, fr.created_at FROM feature_record fr "
				+ "JOIN tenancies t ON t.id = fr.tenancy_id "
				+ "WHERE fr.form_id = ? AND fr.organization_id = ? AND t.user_id = ? ORDER BY fr.created_at DESC";
		List<FeatureRecordSummary> records = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, formId);
			ps.setLong(2, organizationId);
			ps.setLong(3, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					records.add(mapSummary(rs));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return records;
	}

	/** Owner (tenant user id) of a record in a form/organization, or null when not found. */
	public static Long getRecordOwner(Long recordId, Long formId, Long orgId) {
		String sql = "SELECT t.user_id FROM feature_record fr JOIN tenancies t ON t.id = fr.tenancy_id "
				+ "WHERE fr.id = ? AND fr.form_id = ? AND fr.organization_id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, recordId);
			ps.setLong(2, formId);
			ps.setLong(3, orgId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getLong("user_id");
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static List<FieldValue> getByRecordId(Long recordId) {
		String sql = "SELECT cfd.field_label, cfd.field_key, cfd.field_type, cfd.id as field_def_id, cfd.is_required, "
				+ "cfd.default_value, cfd.display_order, fra.column_name, fr.* FROM feature_record fr "
				+ "JOIN field_record_association fra ON fra.form_id = fr.form_id AND fra.is_active = 1 "
				+ "JOIN custom_field_definitions cfd ON cfd.id = fra.field_definition_id AND cfd.is_active = 1 "
				+ "WHERE fr.id = ? ORDER BY cfd.display_order";
		return RecordReader.read(sql, recordId);
	}

	/** Clears one slot column for all records of a form (used when a field is deleted). */
	public static void clearColumn(Connection con, Long formId, String column) throws SQLException {
		if (!column.matches(FieldRecordAssociationDAO.COLUMN_PATTERN))
			throw new IllegalArgumentException("Invalid column: " + column);
		try (PreparedStatement ps = con
				.prepareStatement("UPDATE feature_record SET " + column + " = NULL WHERE form_id = ?")) {
			ps.setLong(1, formId);
			ps.executeUpdate();
		}
	}
}
