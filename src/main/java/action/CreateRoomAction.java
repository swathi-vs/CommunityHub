package action;

import model.Room;
import services.RoomService;

public class CreateRoomAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long propertyId;
	private Room room = new Room();

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	@Override
	public void validate() {
		if (blank(room.getRoomNumber()))
			addFieldError("room.roomNumber", "Room number is required");
		if (room.getCapacity() == null || room.getCapacity() < 1)
			addFieldError("room.capacity", "Capacity must be at least 1");
		if (room.getMonthlyRent() != null && room.getMonthlyRent() < 0)
			addFieldError("room.monthlyRent", "Rent cannot be negative");
	}

	@Override
	public String execute() {
		room.setOrganizationId(organizationId);
		room.setPropertyId(propertyId);
		return respond(RoomService.createRoom(room));
	}
}
