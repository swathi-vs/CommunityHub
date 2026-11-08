package action;

import model.CustomForm;
import services.CustomFormService;
import utils.QueryFilter;

public class GetFormsAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long formId;
	private Boolean isActive;
	private int page = 1, size = 20;
	private String orderBy, orderDir;

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setFormId(Long v) {
		this.formId = v;
	}

	public void setIsActive(Boolean v) {
		this.isActive = v;
	}

	public void setPage(int v) {
		this.page = v;
	}

	public void setSize(int v) {
		this.size = v;
	}

	public void setOrderBy(String v) {
		this.orderBy = v;
	}

	public void setOrderDir(String v) {
		this.orderDir = v;
	}

	@Override
	public String execute() {
		if (formId != null) {
			CustomForm form = CustomFormService.getById(formId, organizationId);
			return form == null ? notFound("Form") : ok("Form", form);
		}
		QueryFilter filter = new QueryFilter("forms").add("is_active", isActive).orderBy(orderBy, orderDir)
				.page(page, size);
		return ok("Forms", CustomFormService.getForms(organizationId, filter));
	}
}
