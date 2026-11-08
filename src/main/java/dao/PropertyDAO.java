package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import model.Property;
import utils.Db;
import utils.Log;
import utils.QueryExecutor;
import utils.QueryFilter;
import utils.UpdateBuilder;

public class PropertyDAO {

	private static Property map(ResultSet rs) throws SQLException {
		Property p = new Property();
		p.setId(rs.getLong("id"));
		p.setOrganizationId(rs.getLong("organization_id"));
		p.setPropertyTypeId(rs.getLong("property_type_id"));
		p.setName(rs.getString("name"));
		p.setAddressLine1(rs.getString("address_line1"));
		p.setCity(rs.getString("city"));
		p.setState(rs.getString("state"));
		p.setPincode(rs.getString("pincode"));
		p.setStatus(rs.getString("status"));
		return p;
	}

	public static Long insertProperty(Connection con, Property property) throws SQLException {
		String query = "insert into properties(organization_id, property_type_id, name, address_line1, city, state, pincode, status) "
				+ "values (?, ?, ?, ?, ?, ?, ?, 'draft')";
		try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, property.getOrganizationId());
			ps.setLong(2, property.getPropertyTypeId());
			ps.setString(3, property.getName());
			ps.setString(4, property.getAddressLine1());
			ps.setString(5, property.getCity());
			ps.setString(6, property.getState());
			ps.setString(7, property.getPincode());
			ps.executeUpdate();
			return Db.generatedId(ps);
		}
	}

	public static Property getById(Long id, Long organizationId) {
		if (id == null || organizationId == null)
			return null;
		String query = "select * from properties where id = ? and organization_id = ? and deleted_at is null";
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

	public static List<Property> getProperties(Long orgId, QueryFilter filter) {
		return QueryExecutor.getList("p.organization_id = ? and p.deleted_at IS NULL", orgId, filter, PropertyDAO::map);
	}

	public static boolean softDelete(Connection con, Long id, Long organizationId) throws SQLException {
		String sql = "UPDATE properties SET deleted_at = NOW() WHERE id = ? AND organization_id = ? AND deleted_at IS NULL";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, organizationId);
			return ps.executeUpdate() > 0;
		}
	}

	public static boolean updateProperty(Property p) {
		return new UpdateBuilder("properties")
				.set("name", p.getName())
				.set("property_type_id", p.getPropertyTypeId())
				.set("address_line1", p.getAddressLine1())
				.set("city", p.getCity())
				.set("state", p.getState())
				.set("pincode", p.getPincode())
				.set("status", p.getStatus())
				.where("id = ? AND organization_id = ? AND deleted_at IS NULL", p.getId(), p.getOrganizationId())
				.execute();
	}
}
