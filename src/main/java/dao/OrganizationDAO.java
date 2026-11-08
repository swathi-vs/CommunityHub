package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Organization;
import utils.Db;
import utils.Log;
import utils.UpdateBuilder;

public class OrganizationDAO {

	public static Long insertOrganization(Connection con, Organization org) throws SQLException {
		String query = "insert into organizations(name,gst_number,business_email,business_phone,address_line1,city,state,pincode,status) "
				+ "values(?,?,?,?,?,?,?,?,?)";
		try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, org.getName());
			ps.setString(2, org.getGstNumber());
			ps.setString(3, org.getBusinessEmail());
			ps.setString(4, org.getBusinessPhone());
			ps.setString(5, org.getAddressLine1());
			ps.setString(6, org.getCity());
			ps.setString(7, org.getState());
			Db.setLong(ps, 8, org.getPincode());
			ps.setString(9, "active");
			ps.executeUpdate();
			return Db.generatedId(ps);
		}
	}

	public static Organization getById(Long id) {
		String query = "select * from organizations where id = ?";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Organization org = new Organization();
					org.setId(rs.getLong("id"));
					org.setName(rs.getString("name"));
					org.setGstNumber(rs.getString("gst_number"));
					org.setBusinessEmail(rs.getString("business_email"));
					org.setBusinessPhone(rs.getString("business_phone"));
					org.setAddressLine1(rs.getString("address_line1"));
					org.setCity(rs.getString("city"));
					org.setState(rs.getString("state"));
					org.setPincode(Db.getLong(rs, "pincode"));
					org.setStatus(rs.getString("status"));
					return org;
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean updateOrganization(Organization o) {
		return new UpdateBuilder("organizations")
				.set("name", o.getName())
				.set("gst_number", o.getGstNumber())
				.set("business_email", o.getBusinessEmail())
				.set("business_phone", o.getBusinessPhone())
				.set("address_line1", o.getAddressLine1())
				.set("city", o.getCity())
				.set("state", o.getState())
				.set("pincode", o.getPincode())
				.where("id = ?", o.getId())
				.execute();
	}
}
