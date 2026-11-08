package action;

import model.CustomForm;
import services.CustomFormService;

public class CreateCustomFormAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private CustomForm customForm = new CustomForm();

	public CustomForm getCustomForm() {
		return customForm;
	}

	public void setCustomForm(CustomForm customForm) {
		this.customForm = customForm;
	}

	@Override
	public void validate() {
		if (blank(customForm.getFormName()))
			addFieldError("customForm.formName", "Form name required");
	}

	@Override
	public String execute() {
		return respond(CustomFormService.createForm(customForm));
	}
}
