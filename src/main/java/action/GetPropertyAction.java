package action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.CustomFieldOption;
import model.FieldValue;
import model.Property;
import services.FormRelationshipService;
import services.PropertyService;
import services.RecordService;

public class GetPropertyAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long propertyId;

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	@Override
	public String execute() {
		Property property = PropertyService.getById(propertyId, organizationId);
		if (property == null)
			return notFound("Property");

		List<FieldValue> fixedFields = new ArrayList<>();
		fixedFields.add(build("name", "Property Name", "text", property.getName()));
		fixedFields.add(build("city", "City", "text", property.getCity()));
		fixedFields.add(build("state", "State", "text", property.getState()));
		fixedFields.add(build("pincode", "Pincode", "text", property.getPincode()));
		fixedFields.add(build("addressLine1", "Address", "text", property.getAddressLine1()));
		FieldValue status = build("status", "Status", "enum", property.getStatus());
		List<CustomFieldOption> statusOptions = new ArrayList<>();
		statusOptions.add(option("draft", "Draft"));
		statusOptions.add(option("active", "Active"));
		statusOptions.add(option("paused", "Paused"));
		statusOptions.add(option("closed", "Closed"));
		status.setOptions(statusOptions);
		fixedFields.add(status);

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("property", property);
		data.put("formId", FormRelationshipService.getFormIdByPropertyId(propertyId));
		data.put("fixedFields", fixedFields);
		data.put("customFields", RecordService.getPropertyRecord(propertyId));
		return ok("Property", data);
	}

	private FieldValue build(String key, String label, String type, String value) {
		FieldValue fv = new FieldValue();
		fv.setFieldKey(key);
		fv.setFieldLabel(label);
		fv.setFieldType(type);
		fv.setValue(value);
		return fv;
	}

	private CustomFieldOption option(String value, String label) {
		CustomFieldOption opt = new CustomFieldOption();
		opt.setOptionValue(value);
		opt.setOptionLabel(label);
		return opt;
	}
}
