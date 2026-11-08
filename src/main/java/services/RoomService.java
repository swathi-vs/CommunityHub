package services;

import java.util.List;

import dao.BedDAO;
import dao.PropertyDAO;
import dao.RoomDAO;
import dao.TenancyDAO;
import model.Room;
import model.ServiceResult;
import utils.Db;

public class RoomService {

	public static ServiceResult createRoom(Room room) {
		if (PropertyDAO.getById(room.getPropertyId(), room.getOrganizationId()) == null)
			return ServiceResult.notFound("Property");
		if (RoomDAO.roomNumberExists(room.getPropertyId(), room.getRoomNumber()))
			return ServiceResult.conflict("Room number already exists in this property");
		Long id = RoomDAO.insertRoom(room);
		if (id == null)
			return ServiceResult.error();
		room.setId(id);
		return ServiceResult.ok("Room created", room);
	}

	public static ServiceResult getRooms(Long organizationId, Long propertyId) {
		if (PropertyDAO.getById(propertyId, organizationId) == null)
			return ServiceResult.notFound("Property");
		return ServiceResult.ok("Rooms", RoomDAO.getRooms(organizationId, propertyId));
	}

	/** A room of the organization that belongs to the given property, or null. */
	public static Room getRoom(Long roomId, Long propertyId, Long organizationId) {
		Room room = RoomDAO.getById(roomId, organizationId);
		return room != null && room.getPropertyId().equals(propertyId) ? room : null;
	}

	/** Soft-deletes the room and its beds. Blocked while the room has active tenancies. */
	public static ServiceResult deleteRoom(Long id, Long propertyId, Long organizationId) {
		if (getRoom(id, propertyId, organizationId) == null)
			return ServiceResult.notFound("Room");
		return Db.tx(con -> {
			if (TenancyDAO.hasActiveForRoom(con, id))
				return ServiceResult.conflict("Room has active tenancies. Terminate them first.");
			BedDAO.softDeleteByRoom(con, id);
			if (!RoomDAO.softDelete(con, id, organizationId))
				return ServiceResult.notFound("Room");
			return ServiceResult.ok("Room deleted");
		});
	}
}
