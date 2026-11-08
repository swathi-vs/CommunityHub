package action;

import services.PropertyTypeService;

public class GetPropertyTypesAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	@Override
	public String execute() {
		return ok("Property types", PropertyTypeService.getPropertyTypes());
	}
}
