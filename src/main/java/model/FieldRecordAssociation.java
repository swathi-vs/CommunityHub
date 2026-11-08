package model;

public class FieldRecordAssociation {

	private Long id;
	private Long formId;
	private Long fieldDefinitionId;
	private String columnName;
	private Boolean isActive;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getFormId() {
		return formId;
	}
	public void setFormId(Long formId) {
		this.formId = formId;
	}
	public Long getFieldDefinitionId() {
		return fieldDefinitionId;
	}
	public void setFieldDefinitionId(Long fieldDefinitionId) {
		this.fieldDefinitionId = fieldDefinitionId;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
}
