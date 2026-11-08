package action;

import services.PropertyService;
import utils.QueryFilter;

public class GetPropertiesAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private String status;
	private Long propertyTypeId;
	private String city;
	private int page = 1;
	private int size = 20;
	private String orderBy;
	private String orderDir;

	public void setOrganizationId(Long o) {
		this.organizationId = o;
	}

	public void setStatus(String s) {
		this.status = s;
	}

	public void setPropertyTypeId(Long p) {
		this.propertyTypeId = p;
	}

	public void setCity(String c) {
		this.city = c;
	}

	public void setPage(int p) {
		this.page = p;
	}

	public void setSize(int s) {
		this.size = s;
	}

	public void setOrderBy(String o) {
		this.orderBy = o;
	}

	public void setOrderDir(String o) {
		this.orderDir = o;
	}

	@Override
	public String execute() {
		QueryFilter filter = new QueryFilter("properties").add("status", status).add("property_type_id", propertyTypeId)
				.add("city", city).orderBy(orderBy, orderDir).page(page, size);
		return ok("Properties", PropertyService.getProperties(organizationId, filter));
	}
}
