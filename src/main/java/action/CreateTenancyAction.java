package action;

import model.Tenancy;
import services.TenancyService;

public class CreateTenancyAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Tenancy tenancy = new Tenancy();

	public Tenancy getTenancy() {
		return tenancy;
	}

	public void setTenancy(Tenancy tenancy) {
		this.tenancy = tenancy;
	}

	@Override
	public void validate() {
		if (tenancy.getRoomId() == null)
			addFieldError("tenancy.roomId", "Room required");
		if (tenancy.getBedId() == null)
			addFieldError("tenancy.bedId", "Bed required");
		if (tenancy.getUserId() == null)
			addFieldError("tenancy.userId", "User required");
		if (tenancy.getStartDate() == null)
			addFieldError("tenancy.startDate", "Start date required");
		if (tenancy.getStartDate() != null && tenancy.getExpectedEndDate() != null
				&& tenancy.getExpectedEndDate().before(tenancy.getStartDate()))
			addFieldError("tenancy.expectedEndDate", "Expected end date must be after start date");
		if (tenancy.getDepositAmount() != null && tenancy.getDepositAmount() < 0)
			addFieldError("tenancy.depositAmount", "Deposit cannot be negative");
		String d = tenancy.getDepositStatus();
		if (d != null && !UpdateTenancyAction.DEPOSIT_STATUSES.contains(d))
			addFieldError("tenancy.depositStatus", "Invalid deposit status");
	}

	@Override
	public String execute() {
		tenancy.setCreatedBy(currentUserId());
		return respond(TenancyService.createTenancy(tenancy));
	}
}
