package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dao.DBConnection;

public class QueryExecutor {

	/**
	 * Runs a filtered, sorted, paged list query.
	 *
	 * @param fixedWhere  trusted SQL condition written by the DAO, using the entity alias
	 * @param fixedParams values for the placeholders in fixedWhere, in order
	 */
	public static <T> List<T> getList(String fixedWhere, List<Object> fixedParams, QueryFilter filter,
			RowMapper<T> mapper) {
		StringBuilder query = new StringBuilder();
		query.append("select ").append(filter.getSelectCols());
		query.append(" from ").append(filter.getTable()).append(" ").append(filter.getAlias());
		query.append(filter.buildJoinClause());
		query.append(" where ").append(fixedWhere);
		query.append(filter.buildWhereClause());
		query.append(" order by ").append(filter.getOrderColumn()).append(" ").append(filter.getOrderDir());
		query.append(" limit ? offset ?");

		List<T> results = new ArrayList<>();
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query.toString())) {
			int i = 1;
			for (Object value : fixedParams)
				ps.setObject(i++, value);
			for (Object value : filter.getParamValues())
				ps.setObject(i++, value);
			ps.setInt(i++, filter.getLimit());
			ps.setInt(i, filter.getOffset());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(mapper.map(rs));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return results;
	}

	public static <T> List<T> getList(String fixedWhere, Long orgId, QueryFilter filter, RowMapper<T> mapper) {
		return getList(fixedWhere, Arrays.asList((Object) orgId), filter, mapper);
	}
}
