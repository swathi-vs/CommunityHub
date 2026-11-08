package action;

import java.util.List;

import model.CustomFieldDefinition;
import model.CustomFieldOption;
import services.CustomFieldDefinitionService;
import services.CustomFormService;

public class CreateFieldDefinitionAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private CustomFieldDefinition fieldDefinition = new CustomFieldDefinition();
	private List<CustomFieldOption> options;

	public CustomFieldDefinition getFieldDefinition() {
		return fieldDefinition;
	}

	public void setFieldDefinition(CustomFieldDefinition fieldDefinition) {
		this.fieldDefinition = fieldDefinition;
	}

	public List<CustomFieldOption> getOptions() {
		return options;
	}

	public void setOptions(List<CustomFieldOption> options) {
		this.options = options;
	}

	@Override
	public void validate() {
		if (fieldDefinition.getFieldKey() == null || !fieldDefinition.getFieldKey().matches("^[a-z][a-z0-9_]{0,49}$"))
			addFieldError("fieldDefinition.fieldKey",
					"Field key required: lowercase letters, digits and underscores, starting with a letter");
		if (blank(fieldDefinition.getFieldLabel()))
			addFieldError("fieldDefinition.fieldLabel", "Field label required");
		if (!CustomFieldDefinitionService.isValidType(fieldDefinition.getFieldType()))
			addFieldError("fieldDefinition.fieldType",
					"Field type must be one of text, enum, number, decimal, boolean, date, time, textarea");
		if ("enum".equals(fieldDefinition.getFieldType())) {
			if (options == null || options.isEmpty()) {
				addFieldError("options", "Options required for enum type");
			} else {
				for (CustomFieldOption o : options) {
					if (o == null || blank(o.getOptionValue()) || blank(o.getOptionLabel())) {
						addFieldError("options", "Each option needs a value and a label");
						break;
					}
				}
			}
		}
	}

	@Override
	public String execute() {
		if (CustomFormService.getActiveById(fieldDefinition.getFormId(), fieldDefinition.getOrganizationId()) == null)
			return notFound("Form");
		return respond(CustomFieldDefinitionService.createFieldDefinition(fieldDefinition, options));
	}
}
