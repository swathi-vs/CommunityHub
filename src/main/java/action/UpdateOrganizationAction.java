package action;

import model.Organization;
import services.OrganizationService;
import utils.ModelMerger;

public class UpdateOrganizationAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Organization organization = new Organization();

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization v) {
		this.organization = v;
	}

	@Override
	public void validate() {
		if (!blank(organization.getBusinessEmail())
				&& !organization.getBusinessEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
			addFieldError("organization.businessEmail", "Invalid email");
		if (!blank(organization.getBusinessPhone()) && !organization.getBusinessPhone().matches("^[0-9]{10}$"))
			addFieldError("organization.businessPhone", "Phone must be 10 digits");
	}

	@Override
	public String execute() {
		Organization existing = OrganizationService.getById(organizationId);
		if (existing == null)
			return notFound("Organization");
		organization.setStatus(null);
		ModelMerger.merge(existing, organization);
		existing.setId(organizationId);
		return OrganizationService.updateOrganization(existing) ? ok("Organization updated", existing) : serverError();
	}
}
