package action;

import model.Property;
import services.PropertyService;
import utils.ModelMerger;

public class UpdatePropertyAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long propertyId;
	private Long organizationId;
	private Property property = new Property();

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property v) {
		this.property = v;
	}

	@Override
	public void validate() {
		if (propertyId == null)
			addFieldError("propertyId", "Property required");
		String s = property.getStatus();
		if (s != null && !java.util.Arrays.asList("draft", "active", "paused", "closed").contains(s))
			addFieldError("property.status", "Status must be draft, active, paused or closed");
	}

	@Override
	public String execute() {
		Property existing = PropertyService.getById(propertyId, organizationId);
		if (existing == null)
			return notFound("Property");
		ModelMerger.merge(existing, property);
		existing.setId(propertyId);
		existing.setOrganizationId(organizationId);
		return respond(PropertyService.updateProperty(existing));
	}
}
