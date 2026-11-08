package action;

import services.PropertyService;

public class DeletePropertyAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long propertyId;
	private Long organizationId;

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	@Override
	public void validate() {
		if (propertyId == null)
			addFieldError("propertyId", "Property required");
	}

	@Override
	public String execute() {
		return respond(PropertyService.deleteProperty(propertyId, organizationId));
	}
}
