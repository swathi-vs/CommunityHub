package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import dao.DBConnection;
import model.ServiceResult;

/** JDBC helpers: null-safe parameter binding, nullable reads and transactions. */
public final class Db {

	private Db() {
	}

	@FunctionalInterface
	public interface Work {
		ServiceResult run(Connection con) throws Exception;
	}

	/**
	 * Runs the work in one transaction. Commits when the result is successful,
	 * rolls back when it is not or when an exception is thrown.
	 */
	public static ServiceResult tx(Work work) {
		try (Connection con = DBConnection.getConnection()) {
			con.setAutoCommit(false);
			try {
				ServiceResult result = work.run(con);
				if (result != null && result.isSuccess())
					con.commit();
				else
					con.rollback();
				return result;
			} catch (Exception e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		} catch (Exception e) {
			Log.error(e);
			return ServiceResult.error();
		}
	}

	public static void setLong(PreparedStatement ps, int i, Long v) throws SQLException {
		if (v == null)
			ps.setNull(i, Types.BIGINT);
		else
			ps.setLong(i, v);
	}

	public static void setDouble(PreparedStatement ps, int i, Double v) throws SQLException {
		if (v == null)
			ps.setNull(i, Types.DECIMAL);
		else
			ps.setDouble(i, v);
	}

	public static void setInt(PreparedStatement ps, int i, Integer v) throws SQLException {
		if (v == null)
			ps.setNull(i, Types.INTEGER);
		else
			ps.setInt(i, v);
	}

	public static void setBoolean(PreparedStatement ps, int i, Boolean v) throws SQLException {
		if (v == null)
			ps.setNull(i, Types.BOOLEAN);
		else
			ps.setBoolean(i, v);
	}

	public static Long getLong(ResultSet rs, String col) throws SQLException {
		long v = rs.getLong(col);
		return rs.wasNull() ? null : v;
	}

	public static Double getDouble(ResultSet rs, String col) throws SQLException {
		double v = rs.getDouble(col);
		return rs.wasNull() ? null : v;
	}

	public static Long generatedId(PreparedStatement ps) throws SQLException {
		try (ResultSet rs = ps.getGeneratedKeys()) {
			return rs.next() ? rs.getLong(1) : null;
		}
	}

	public static double money(double v) {
		return Math.round(v * 100.0) / 100.0;
	}
}
