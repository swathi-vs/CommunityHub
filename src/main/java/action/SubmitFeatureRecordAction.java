package action;

import services.CustomFormService;
import services.RecordService;
import services.TenancyService;

public class SubmitFeatureRecordAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long formId;
	private Long organizationId;
	private String payload;

	public void setFormId(Long v) {
		this.formId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setPayload(String v) {
		this.payload = v;
	}

	@Override
	public void validate() {
		if (formId == null)
			addFieldError("formId", "Form required");
		if (blank(payload))
			addFieldError("payload", "No data submitted");
	}

	@Override
	public String execute() {
		if (CustomFormService.getActiveById(formId, organizationId) == null)
			return notFound("Form");
		Long tenancyId = TenancyService.getActiveTenancyId(currentUserId(), organizationId);
		if (tenancyId == null)
			return badRequest("You need an active tenancy in this organization to submit this form");
		return respond(RecordService.saveFeatureRecord(formId, organizationId, tenancyId, payload));
	}
}
