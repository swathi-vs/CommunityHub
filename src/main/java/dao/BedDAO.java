package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Bed;
import utils.Db;
import utils.Log;

public class BedDAO {

	public static final String AVAILABLE = "available";
	public static final String OCCUPIED = "occupied";
	public static final String MAINTENANCE = "maintenance";

	private static Bed map(ResultSet rs) throws SQLException {
		Bed bed = new Bed();
		bed.setId(rs.getLong("id"));
		bed.setRoomId(rs.getLong("room_id"));
		bed.setBedLabel(rs.getString("bed_label"));
		bed.setStatus(rs.getString("status"));
		return bed;
	}

	public static Long insertBed(Bed bed, Long organizationId, Long propertyId) {
		String query = "INSERT INTO beds (organization_id, property_id, room_id, bed_label, status) "
				+ "VALUES (?, ?, ?, ?, 'available')";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, organizationId);
			ps.setLong(2, propertyId);
			ps.setLong(3, bed.getRoomId());
			ps.setString(4, bed.getBedLabel());
			ps.executeUpdate();
			return Db.generatedId(ps);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	/** Non-deleted beds of a room, optionally filtered by status. */
	public static List<Bed> getBeds(Long roomId, String status) {
		List<Bed> beds = new ArrayList<>();
		String query = "select id, room_id, bed_label, status from beds where room_id = ? and deleted_at is null"
				+ (status != null ? " and status = ?" : "") + " order by bed_label";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, roomId);
			if (status != null)
				ps.setString(2, status);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					beds.add(map(rs));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return beds;
	}

	/** A non-deleted bed whose room belongs to the organization, or null. */
	public static Bed getById(Long bedId, Long organizationId) {
		String query = "select b.id, b.room_id, b.bed_label, b.status from beds b join rooms r on r.id = b.room_id "
				+ "where b.id = ? and r.organization_id = ? and b.deleted_at is null and r.deleted_at is null";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, bedId);
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

	/** Locks a non-deleted bed row for the rest of the transaction. */
	public static Bed lockBed(Connection con, Long bedId) throws SQLException {
		if (bedId == null)
			return null;
		String query = "select id, room_id, bed_label, status from beds where id = ? and deleted_at is null for update";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, bedId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	public static void setStatus(Connection con, Long bedId, String status) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement("update beds set status = ? where id = ?")) {
			ps.setString(1, status);
			ps.setLong(2, bedId);
			ps.executeUpdate();
		}
	}

	public static boolean updateBed(Connection con, Bed b) throws SQLException {
		StringBuilder sql = new StringBuilder("update beds set ");
		List<Object> params = new ArrayList<>();
		if (b.getBedLabel() != null && !b.getBedLabel().trim().isEmpty()) {
			sql.append("bed_label = ?");
			params.add(b.getBedLabel());
		}
		if (b.getStatus() != null) {
			if (!params.isEmpty())
				sql.append(", ");
			sql.append("status = ?");
			params.add(b.getStatus());
		}
		if (params.isEmpty())
			return false;
		sql.append(" where id = ?");
		params.add(b.getId());
		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++)
				ps.setObject(i + 1, params.get(i));
			return ps.executeUpdate() > 0;
		}
	}

	public static void softDeleteByRoom(Connection con, Long roomId) throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("update beds set deleted_at = NOW() where room_id = ? and deleted_at is null")) {
			ps.setLong(1, roomId);
			ps.executeUpdate();
		}
	}

	public static void softDeleteByProperty(Connection con, Long propertyId) throws SQLException {
		String sql = "update beds b join rooms r on r.id = b.room_id set b.deleted_at = NOW() "
				+ "where r.property_id = ? and b.deleted_at is null";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, propertyId);
			ps.executeUpdate();
		}
	}
}
