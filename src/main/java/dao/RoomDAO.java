package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Room;
import utils.Db;
import utils.Log;

public class RoomDAO {

	private static Room map(ResultSet rs) throws SQLException {
		Room room = new Room();
		room.setId(rs.getLong("id"));
		room.setOrganizationId(rs.getLong("organization_id"));
		room.setPropertyId(rs.getLong("property_id"));
		room.setRoomNumber(rs.getString("room_number"));
		room.setRoomType(rs.getString("room_type"));
		room.setCapacity(rs.getInt("capacity"));
		room.setMonthlyRent(rs.getDouble("monthly_rent"));
		room.setHasAc(rs.getBoolean("has_ac"));
		room.setStatus(rs.getString("status"));
		return room;
	}

	public static Long insertRoom(Room room) {
		String query = "INSERT INTO rooms (organization_id, property_id, room_number, room_type, capacity, monthly_rent, has_ac) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, room.getOrganizationId());
			ps.setLong(2, room.getPropertyId());
			ps.setString(3, room.getRoomNumber());
			ps.setString(4, room.getRoomType());
			ps.setInt(5, room.getCapacity());
			ps.setDouble(6, room.getMonthlyRent() == null ? 0 : room.getMonthlyRent());
			ps.setBoolean(7, Boolean.TRUE.equals(room.getHasAc()));
			ps.executeUpdate();
			return Db.generatedId(ps);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean roomNumberExists(Long propertyId, String roomNumber) {
		String query = "select 1 from rooms where property_id = ? and room_number = ? and deleted_at is null";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, propertyId);
			ps.setString(2, roomNumber);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}

	public static List<Room> getRooms(Long organizationId, Long propertyId) {
		List<Room> rooms = new ArrayList<>();
		String query = "select * from rooms "
				+ "where organization_id = ? and property_id = ? and deleted_at is null order by room_number";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, organizationId);
			ps.setLong(2, propertyId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					rooms.add(map(rs));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return rooms;
	}

	/** A non-deleted room of the organization, or null. */
	public static Room getById(Long roomId, Long organizationId) {
		try (Connection con = DBConnection.getConnection()) {
			return getById(con, roomId, organizationId);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static Room getById(Connection con, Long roomId, Long organizationId) throws SQLException {
		if (roomId == null || organizationId == null)
			return null;
		String query = "select * from rooms "
				+ "where id = ? and organization_id = ? and deleted_at is null";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setLong(1, roomId);
			ps.setLong(2, organizationId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	public static boolean softDelete(Connection con, Long id, Long organizationId) throws SQLException {
		String sql = "UPDATE rooms SET deleted_at = NOW() WHERE id = ? AND organization_id = ? AND deleted_at IS NULL";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, organizationId);
			return ps.executeUpdate() > 0;
		}
	}

	public static void softDeleteByProperty(Connection con, Long propertyId) throws SQLException {
		String sql = "UPDATE rooms SET deleted_at = NOW() WHERE property_id = ? AND deleted_at IS NULL";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, propertyId);
			ps.executeUpdate();
		}
	}
}
