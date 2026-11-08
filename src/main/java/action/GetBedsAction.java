package action;

import services.BedService;

public class GetBedsAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long propertyId;
	private Long roomId;
	private String status;

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String execute() {
		return respond(BedService.getBeds(roomId, propertyId, organizationId, blank(status) ? null : status));
	}
}
