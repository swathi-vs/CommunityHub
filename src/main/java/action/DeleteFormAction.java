package action;

import services.CustomFormService;

public class DeleteFormAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long formId;
	private Long organizationId;

	public void setFormId(Long v) {
		this.formId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	@Override
	public void validate() {
		if (formId == null)
			addFieldError("formId", "Form required");
	}

	@Override
	public String execute() {
		return respond(CustomFormService.deactivateForm(formId, organizationId));
	}
}
