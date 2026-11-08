package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.CustomFieldOption;
import utils.Db;
import utils.Log;

public class CustomFieldOptionDAO {

	public static Long createFieldOption(Connection con, CustomFieldOption option) throws SQLException {
		String query = "insert into custom_field_options(field_definition_id, option_value, option_label, display_order) values (?,?,?,?)";
		try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, option.getFieldDefinitionId());
			ps.setString(2, option.getOptionValue());
			ps.setString(3, option.getOptionLabel());
			ps.setInt(4, option.getDisplayOrder() == null ? 0 : option.getDisplayOrder());
			ps.executeUpdate();
			return Db.generatedId(ps);
		}
	}

	public static Long createFieldOption(CustomFieldOption option) {
		try (Connection con = DBConnection.getConnection()) {
			return createFieldOption(con, option);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static List<CustomFieldOption> getByFieldDefinitionId(Long fieldDefinitionId) {
		String sql = "SELECT * FROM custom_field_options WHERE field_definition_id = ? ORDER BY display_order";
		List<CustomFieldOption> options = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, fieldDefinitionId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CustomFieldOption opt = new CustomFieldOption();
					opt.setId(rs.getLong("id"));
					opt.setFieldDefinitionId(rs.getLong("field_definition_id"));
					opt.setOptionValue(rs.getString("option_value"));
					opt.setOptionLabel(rs.getString("option_label"));
					opt.setDisplayOrder(rs.getInt("display_order"));
					options.add(opt);
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return options;
	}
}
