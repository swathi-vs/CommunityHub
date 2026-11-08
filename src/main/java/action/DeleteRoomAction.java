package action;

import services.RoomService;

public class DeleteRoomAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long roomId;
	private Long propertyId;
	private Long organizationId;

	public void setRoomId(Long v) {
		this.roomId = v;
	}

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	@Override
	public void validate() {
		if (roomId == null)
			addFieldError("roomId", "Room required");
	}

	@Override
	public String execute() {
		return respond(RoomService.deleteRoom(roomId, propertyId, organizationId));
	}
}
