package action;

import services.RoomService;

public class GetRoomAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long propertyId;

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	@Override
	public String execute() {
		return respond(RoomService.getRooms(organizationId, propertyId));
	}
}
