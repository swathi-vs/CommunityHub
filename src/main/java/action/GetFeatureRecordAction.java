package action;

import services.FeatureRecordService;
import utils.TenantScoped;

public class GetFeatureRecordAction extends BaseAction implements TenantScoped {

	private static final long serialVersionUID = 1L;

	private Long recordId;
	private Long organizationId;
	private Long formId;
	private Long scopedUserId;

	public void setRecordId(Long v) {
		this.recordId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setFormId(Long v) {
		this.formId = v;
	}

	@Override
	public void setScopedUserId(Long v) {
		this.scopedUserId = v;
	}

	@Override
	public String execute() {
		Long owner = FeatureRecordService.getRecordOwner(recordId, formId, organizationId);
		if (owner == null || (scopedUserId != null && !scopedUserId.equals(owner)))
			return notFound("Record");
		return ok("Record", FeatureRecordService.getById(recordId));
	}
}
