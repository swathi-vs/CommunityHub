package utils;

import dao.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class UpdateBuilder {

	private final String table;
	private final StringJoiner sj = new StringJoiner(", ");
	private final List<Object> params = new ArrayList<>();
	private final List<Object> whereParams = new ArrayList<>();
	private String whereClause = "";

	public UpdateBuilder(String table) {
		this.table = table;
	}

	public UpdateBuilder set(String column, Object value) {
		if (value == null)
			return this;
		if (value instanceof String && ((String) value).trim().isEmpty())
			return this;
		sj.add(column + " = ?");
		params.add(value);
		return this;
	}

	public UpdateBuilder where(String clause, Object... values) {
		this.whereClause = clause;
		for (Object v : values)
			whereParams.add(v);
		return this;
	}

	public boolean execute() {
		if (params.isEmpty())
			return false;
		String sql = "UPDATE " + table + " SET " + sj + " WHERE " + whereClause;
		List<Object> all = new ArrayList<>(params);
		all.addAll(whereParams);
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < all.size(); i++)
				ps.setObject(i + 1, all.get(i));
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}
}