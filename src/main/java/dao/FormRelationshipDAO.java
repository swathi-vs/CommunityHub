package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import utils.Log;

public class FormRelationshipDAO {

	/** Makes formId the property's only active form. Details saved for a previous form are removed. */
	public static void associate(Connection con, Long propertyId, Long formId) throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("delete from property_record where property_id = ? and form_id <> ?")) {
			ps.setLong(1, propertyId);
			ps.setLong(2, formId);
			ps.executeUpdate();
		}
		try (PreparedStatement ps = con
				.prepareStatement("update form_relationships set is_active = 0 where property_id = ? and form_id <> ?")) {
			ps.setLong(1, propertyId);
			ps.setLong(2, formId);
			ps.executeUpdate();
		}
		int updated;
		try (PreparedStatement ps = con
				.prepareStatement("update form_relationships set is_active = 1 where property_id = ? and form_id = ?")) {
			ps.setLong(1, propertyId);
			ps.setLong(2, formId);
			updated = ps.executeUpdate();
		}
		if (updated == 0) {
			try (PreparedStatement ps = con.prepareStatement(
					"insert into form_relationships(property_id, form_id, is_active) values (?,?,1)")) {
				ps.setLong(1, propertyId);
				ps.setLong(2, formId);
				ps.executeUpdate();
			}
		}
	}

	public static void deactivateForProperty(Connection con, Long propertyId) throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("update form_relationships set is_active = 0 where property_id = ?")) {
			ps.setLong(1, propertyId);
			ps.executeUpdate();
		}
	}

	public static void deactivateForForm(Connection con, Long formId) throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("update form_relationships set is_active = 0 where form_id = ?")) {
			ps.setLong(1, formId);
			ps.executeUpdate();
		}
	}

	public static Long getFormIdByPropertyId(Long propertyId) {
		String query = "select form_id from form_relationships where property_id = ? and is_active = 1 order by id desc limit 1";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, propertyId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getLong("form_id");
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}
}
