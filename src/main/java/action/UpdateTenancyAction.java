package action;

import java.util.Arrays;
import java.util.List;

import model.Tenancy;
import services.TenancyService;
import utils.ModelMerger;

public class UpdateTenancyAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	static final List<String> STATUSES = Arrays.asList("active", "vacated", "terminated");
	static final List<String> DEPOSIT_STATUSES = Arrays.asList("held", "refunded", "forfeited");

	private Long organizationId;
	private Long tenancyId;
	private Tenancy tenancy = new Tenancy();

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setTenancyId(Long v) {
		this.tenancyId = v;
	}

	public Tenancy getTenancy() {
		return tenancy;
	}

	public void setTenancy(Tenancy v) {
		this.tenancy = v;
	}

	@Override
	public void validate() {
		if (tenancy.getStatus() != null && !STATUSES.contains(tenancy.getStatus()))
			addFieldError("tenancy.status", "Invalid status");
		if (tenancy.getDepositStatus() != null && !DEPOSIT_STATUSES.contains(tenancy.getDepositStatus()))
			addFieldError("tenancy.depositStatus", "Invalid deposit status");
		if (tenancy.getDepositAmount() != null && tenancy.getDepositAmount() < 0)
			addFieldError("tenancy.depositAmount", "Deposit cannot be negative");
	}

	@Override
	public String execute() {
		Tenancy existing = TenancyService.getById(tenancyId, organizationId);
		if (existing == null)
			return notFound("Tenancy");
		// Organization, property and tenant of a tenancy cannot be changed.
		tenancy.setOrganizationId(null);
		tenancy.setPropertyId(null);
		tenancy.setUserId(null);
		tenancy.setCreatedBy(null);
		ModelMerger.merge(existing, tenancy);
		existing.setId(tenancyId);
		existing.setOrganizationId(organizationId);
		return respond(TenancyService.updateTenancy(existing));
	}
}
