package action;

import model.CustomFieldOption;
import services.CustomFieldOptionService;

public class CreateFieldOptionAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private CustomFieldOption fieldOption = new CustomFieldOption();
	private Long organizationId;
	private Long formId;

	public CustomFieldOption getFieldOption() {
		return fieldOption;
	}

	public void setFieldOption(CustomFieldOption fieldOption) {
		this.fieldOption = fieldOption;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	@Override
	public void validate() {
		if (blank(fieldOption.getOptionValue()))
			addFieldError("fieldOption.optionValue", "Option value required");
		if (blank(fieldOption.getOptionLabel()))
			addFieldError("fieldOption.optionLabel", "Option label required");
	}

	@Override
	public String execute() {
		return respond(CustomFieldOptionService.createFieldOption(fieldOption, formId, organizationId));
	}
}
