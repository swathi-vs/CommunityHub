package action;

import model.FormRelationship;
import services.FormRelationshipService;

public class AssociateFormToPropertyAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private FormRelationship formRelationship = new FormRelationship();
	private Long organizationId;

	public FormRelationship getFormRelationship() {
		return formRelationship;
	}

	public void setFormRelationship(FormRelationship formRelationship) {
		this.formRelationship = formRelationship;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	@Override
	public void validate() {
		if (formRelationship.getPropertyId() == null)
			addFieldError("formRelationship.propertyId", "Property required");
		if (formRelationship.getFormId() == null)
			addFieldError("formRelationship.formId", "Form required");
	}

	@Override
	public String execute() {
		return respond(FormRelationshipService.associate(formRelationship.getPropertyId(),
				formRelationship.getFormId(), organizationId));
	}
}
