package action;

import services.FormRelationshipService;
import services.PropertyService;
import services.RecordService;

public class SubmitPropertyRecordAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long propertyId;
	private Long organizationId;
	private String payload;

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setPayload(String v) {
		this.payload = v;
	}

	@Override
	public void validate() {
		if (propertyId == null)
			addFieldError("propertyId", "Property required");
		if (blank(payload))
			addFieldError("payload", "No data submitted");
	}

	@Override
	public String execute() {
		if (PropertyService.getById(propertyId, organizationId) == null)
			return notFound("Property");
		Long formId = FormRelationshipService.getFormIdByPropertyId(propertyId);
		if (formId == null)
			return badRequest("No form is linked to this property. Link a form first.");
		return respond(RecordService.savePropertyRecord(propertyId, organizationId, formId, payload));
	}
}
