package model;

import java.util.List;

public class FieldValue {

	private Long fieldDefId;
	private String fieldKey;
	private String fieldLabel;
	private String fieldType;
	private Boolean isRequired;
	private String defaultValue;
	private Integer displayOrder;
	private String columnName;
	private String value;
	private List<CustomFieldOption> options;

	public Long getFieldDefId() {
		return fieldDefId;
	}
	public void setFieldDefId(Long v) {
		this.fieldDefId = v;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public void setFieldKey(String v) {
		this.fieldKey = v;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String v) {
		this.fieldLabel = v;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String v) {
		this.fieldType = v;
	}

	public Boolean getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(Boolean v) {
		this.isRequired = v;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String v) {
		this.defaultValue = v;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer v) {
		this.displayOrder = v;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String v) {
		this.columnName = v;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String v) {
		this.value = v;
	}

	public List<CustomFieldOption> getOptions() {
		return options;
	}

	public void setOptions(List<CustomFieldOption> v) {
		this.options = v;
	}
}