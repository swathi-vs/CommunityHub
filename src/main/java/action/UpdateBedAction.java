package action;

import model.Bed;
import services.BedService;

public class UpdateBedAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long bedId;
	private Bed bed = new Bed();

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public void setBedId(Long v) {
		this.bedId = v;
	}

	public Bed getBed() {
		return bed;
	}

	public void setBed(Bed v) {
		this.bed = v;
	}

	@Override
	public void validate() {
		String s = bed.getStatus();
		if (s != null && !s.equals("available") && !s.equals("maintenance"))
			addFieldError("bed.status", "Status must be available or maintenance");
	}

	@Override
	public String execute() {
		bed.setId(bedId);
		bed.setRoomId(null);
		return respond(BedService.updateBed(bed, organizationId));
	}
}
