package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.PropertyType;
import utils.Log;

public class PropertyTypeDAO {

	public static List<PropertyType> getPropertyTypes() {
		List<PropertyType> propertyTypes = new ArrayList<>();
		String query = "select * from property_types order by id";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				PropertyType propertyType = new PropertyType();
				propertyType.setId(rs.getLong("id"));
				propertyType.setName(rs.getString("name"));
				propertyTypes.add(propertyType);
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return propertyTypes;
	}

	public static boolean exists(Long id) {
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement("select 1 from property_types where id = ?")) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}
}
