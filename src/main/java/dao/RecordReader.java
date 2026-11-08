package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.FieldValue;
import utils.Log;

/** Turns a record row joined with its field mappings into labelled field values. */
final class RecordReader {

	private RecordReader() {
	}

	static List<FieldValue> read(String sql, Object... params) {
		List<FieldValue> list = new ArrayList<>();
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++)
				ps.setObject(i + 1, params[i]);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String col = rs.getString("column_name");
					if (col == null || !col.matches(FieldRecordAssociationDAO.COLUMN_PATTERN))
						continue;
					FieldValue fv = new FieldValue();
					fv.setFieldDefId(rs.getLong("field_def_id"));
					fv.setFieldLabel(rs.getString("field_label"));
					fv.setFieldKey(rs.getString("field_key"));
					fv.setFieldType(rs.getString("field_type"));
					fv.setIsRequired(rs.getBoolean("is_required"));
					fv.setDefaultValue(rs.getString("default_value"));
					fv.setDisplayOrder(rs.getInt("display_order"));
					fv.setColumnName(col);
					fv.setValue(rs.getString(col));
					list.add(fv);
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return list;
	}
}
