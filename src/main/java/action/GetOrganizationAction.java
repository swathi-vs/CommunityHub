package action;

import model.Organization;
import services.OrganizationService;

public class GetOrganizationAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	@Override
	public String execute() {
		Organization organization = OrganizationService.getById(organizationId);
		return organization == null ? notFound("Organization") : ok("Organization", organization);
	}
}
