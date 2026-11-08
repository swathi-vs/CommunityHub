package action;

import services.CustomFieldDefinitionService;

public class DeleteFieldDefinitionAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long fieldDefinitionId;
	private Long formId;
	private Long organizationId;

	public void setFieldDefinitionId(Long v) {
		this.fieldDefinitionId = v;
	}

	public void setFormId(Long v) {
		this.formId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	@Override
	public void validate() {
		if (fieldDefinitionId == null)
			addFieldError("fieldDefinitionId", "Field definition required");
	}

	@Override
	public String execute() {
		return respond(CustomFieldDefinitionService.deactivateFieldDefinition(fieldDefinitionId, formId, organizationId));
	}
}
