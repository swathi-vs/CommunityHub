package action;

import model.Organization;
import services.OrganizationService;

public class CreateOrganizationAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Organization organization = new Organization();

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@Override
	public void validate() {
		if (blank(organization.getName()))
			addFieldError("organization.name", "Organization name required");
		if (organization.getBusinessEmail() == null
				|| !organization.getBusinessEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
			addFieldError("organization.businessEmail", "Invalid email");
		if (organization.getBusinessPhone() == null || !organization.getBusinessPhone().matches("^[0-9]{10}$"))
			addFieldError("organization.businessPhone", "Invalid phone");
	}

	@Override
	public String execute() {
		return respond(OrganizationService.createOrganization(organization, currentUserId()));
	}
}
