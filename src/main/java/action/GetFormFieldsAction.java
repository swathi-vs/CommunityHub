package action;

import services.CustomFieldDefinitionService;
import services.CustomFormService;

public class GetFormFieldsAction extends BaseAction {

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
	public String execute() {
		if (CustomFormService.getById(formId, organizationId) == null)
			return notFound("Form");
		return ok("Form fields", CustomFieldDefinitionService.getByFormId(formId));
	}
}
