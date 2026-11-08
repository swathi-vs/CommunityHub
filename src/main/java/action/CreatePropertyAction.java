package action;

import model.Property;
import services.PropertyService;

public class CreatePropertyAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Property property = new Property();
	private String payload;
	private Long formId;

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public void setPayload(String v) {
		this.payload = v;
	}

	public void setFormId(Long v) {
		this.formId = v;
	}

	@Override
	public void validate() {
		if (blank(property.getName()))
			addFieldError("property.name", "Property name required");
		if (blank(property.getAddressLine1()))
			addFieldError("property.addressLine1", "Address required");
		if (blank(property.getCity()))
			addFieldError("property.city", "City required");
		if (blank(property.getState()))
			addFieldError("property.state", "State required");
		if (property.getPincode() == null || !property.getPincode().matches("^[0-9]{6}$"))
			addFieldError("property.pincode", "Pincode must be 6 digits");
		if (property.getPropertyTypeId() == null)
			addFieldError("property.propertyTypeId", "Property type required");
		if (property.getOrganizationId() == null)
			addFieldError("property.organizationId", "Organization required");
		if (!blank(payload) && formId == null)
			addFieldError("formId", "Form required when submitting property details");
	}

	@Override
	public String execute() {
		return respond(PropertyService.createProperty(property, formId, payload));
	}
}
