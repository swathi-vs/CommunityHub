package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import model.Tenancy;
import utils.Db;
import utils.Log;
import utils.QueryExecutor;
import utils.QueryFilter;

public class TenancyDAO {

	public static final String ACTIVE = "active";
	public static final String TERMINATED = "terminated";

	private static Tenancy map(ResultSet rs) throws SQLException {
		Tenancy t = new Tenancy();
		t.setId(rs.getLong("id"));
		t.setOrganizationId(rs.getLong("organization_id"));
		t.setPropertyId(rs.getLong("property_id"));
		t.setUserId(rs.getLong("user_id"));
		t.setRoomId(rs.getLong("room_id"));
		t.setBedId(Db.getLong(rs, "bed_id"));
		t.setStartDate(rs.getDate("start_date"));
		t.setExpectedEndDate(rs.getDate("expected_end_date"));
		t.setDepositAmount(Db.getDouble(rs, "deposit_amount"));
		t.setDepositStatus(rs.getString("deposit_status"));
		t.setStatus(rs.getString("status"));
		return t;
	}

	public static Long createTenancy(Connection con, Tenancy tenancy) throws SQLException {
		String query = "insert into tenancies(organization_id, property_id, room_id, bed_id, user_id, start_date, "
				+ "expected_end_date, deposit_amount, deposit_status, status, created_by) values (?,?,?,?,?,?,?,?,?,?,?)";
		try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, tenancy.getOrganizationId());
			ps.setLong(2, tenancy.getPropertyId());
			ps.setLong(3, tenancy.getRoomId());
			Db.setLong(ps, 4, tenancy.getBedId());
			ps.setLong(5, tenancy.getUserId());
			ps.setDate(6, tenancy.getStartDate());
			ps.setDate(7, tenancy.getExpectedEndDate());
			Db.setDouble(ps, 8, tenancy.getDepositAmount());
			ps.setString(9, tenancy.getDepositStatus());
			ps.setString(10, tenancy.getStatus());
			Db.setLong(ps, 11, tenancy.getCreatedBy());
			ps.executeUpdate();
			return Db.generatedId(ps);
		}
	}

	public static Tenancy getById(Long id, Long orgId) {
		try (Connection con = DBConnection.getConnection()) {
			return getById(con, id, orgId, false);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static Tenancy getById(Connection con, Long id, Long orgId, boolean forUpdate) throws SQLException {
		if (id == null || orgId == null)
			return null;
		String sql = "SELECT * FROM tenancies WHERE id = ? AND organization_id = ?" + (forUpdate ? " FOR UPDATE" : "");
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, orgId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	/** Most recent active tenancy of the user in the organization, or null. */
	public static Tenancy getActiveTenancy(Long userId, Long organizationId) {
		String query = "select * from tenancies where user_id = ? and organization_id = ? and status = 'active' "
				+ "order by start_date desc limit 1";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, userId);
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

	public static Long getActiveTenancyId(Long userId, Long organizationId) {
		Tenancy t = getActiveTenancy(userId, organizationId);
		return t == null ? null : t.getId();
	}

	private static boolean any(Connection con, String sql, Object... params) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++)
				ps.setObject(i + 1, params[i]);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/** Tenancies that hold a bed. */
	private static final String HOLDING = "status = 'active' and deleted_at is null";

	public static boolean hasActiveForUser(Connection con, Long userId, Long orgId) throws SQLException {
		return any(con, "select 1 from tenancies where user_id = ? and organization_id = ? and " + HOLDING + " limit 1",
				userId, orgId);
	}

	public static boolean hasActiveForRoom(Connection con, Long roomId) throws SQLException {
		return any(con, "select 1 from tenancies where room_id = ? and " + HOLDING + " limit 1", roomId);
	}

	public static boolean hasActiveForProperty(Connection con, Long propertyId) throws SQLException {
		return any(con, "select 1 from tenancies where property_id = ? and " + HOLDING + " limit 1", propertyId);
	}

	public static boolean hasActiveForBed(Connection con, Long bedId) throws SQLException {
		return any(con, "select 1 from tenancies where bed_id = ? and " + HOLDING + " limit 1", bedId);
	}

	public static Long getUserIdByTenancy(Long tenancyId, Long organizationId) {
		Tenancy t = getById(tenancyId, organizationId);
		return t == null ? null : t.getUserId();
	}

	public static List<Tenancy> getTenancies(Long orgId, QueryFilter filter) {
		return QueryExecutor.getList("t.organization_id = ?", orgId, filter, TenancyDAO::map);
	}

	public static boolean updateTenancy(Connection con, Tenancy t) throws SQLException {
		String sql = "update tenancies set room_id = ?, bed_id = ?, start_date = ?, expected_end_date = ?, "
				+ "deposit_amount = ?, deposit_status = ?, status = ? where id = ? and organization_id = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, t.getRoomId());
			Db.setLong(ps, 2, t.getBedId());
			ps.setDate(3, t.getStartDate());
			ps.setDate(4, t.getExpectedEndDate());
			Db.setDouble(ps, 5, t.getDepositAmount());
			ps.setString(6, t.getDepositStatus());
			ps.setString(7, t.getStatus());
			ps.setLong(8, t.getId());
			ps.setLong(9, t.getOrganizationId());
			return ps.executeUpdate() > 0;
		}
	}
}
