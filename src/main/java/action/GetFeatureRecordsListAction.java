package action;

import services.CustomFormService;
import services.FeatureRecordService;
import utils.TenantScoped;

public class GetFeatureRecordsListAction extends BaseAction implements TenantScoped {

	private static final long serialVersionUID = 1L;

	private Long formId;
	private Long organizationId;
	private Long scopedUserId;

	public void setFormId(Long v) {
		this.formId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	@Override
	public void setScopedUserId(Long v) {
		this.scopedUserId = v;
	}

	@Override
	public String execute() {
		if (CustomFormService.getById(formId, organizationId) == null)
			return notFound("Form");
		if (scopedUserId != null)
			return ok("Records", FeatureRecordService.getByFormAndUser(formId, organizationId, scopedUserId));
		return ok("Records", FeatureRecordService.getByFormId(formId, organizationId));
	}
}
