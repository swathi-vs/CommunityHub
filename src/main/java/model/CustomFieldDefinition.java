package model;

import java.util.List;

public class CustomFieldDefinition {

	private Long id;
	private Long organizationId;
	private Long formId;
	private String fieldKey;
	private String fieldLabel;
	private String fieldType;
	private Boolean isRequired;
	private String defaultValue;
	private Integer displayOrder;
	private String helpText;
	private Boolean isActive;
	private List<CustomFieldOption> options;
	private String columnName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long v) {
		this.formId = v;
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

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String v) {
		this.helpText = v;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean v) {
		this.isActive = v;
	}

	public List<CustomFieldOption> getOptions() {
		return options;
	}

	public void setOptions(List<CustomFieldOption> v) {
		this.options = v;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String v) {
		this.columnName = v;
	}
}
