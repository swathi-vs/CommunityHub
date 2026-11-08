package action;

import model.Bed;
import services.BedService;

public class CreateBedAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long propertyId;
	private Long roomId;
	private Bed bed = new Bed();

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Bed getBed() {
		return bed;
	}

	public void setBed(Bed bed) {
		this.bed = bed;
	}

	@Override
	public void validate() {
		if (blank(bed.getBedLabel()))
			addFieldError("bed.bedLabel", "Bed label is required");
	}

	@Override
	public String execute() {
		bed.setRoomId(roomId);
		return respond(BedService.createBed(bed, propertyId, organizationId));
	}
}
