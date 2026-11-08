package model;

import java.sql.Timestamp;

public class CustomFieldOption {
	private Long id;
	private Long fieldDefinitionId;
	private String optionValue;
	private String optionLabel;
	private Integer displayOrder;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getFieldDefinitionId() {
		return fieldDefinitionId;
	}
	public void setFieldDefinitionId(Long fieldDefinitionId) {
		this.fieldDefinitionId = fieldDefinitionId;
	}
	public String getOptionValue() {
		return optionValue;
	}
	public void setOptionValue(String optionValue) {
		this.optionValue = optionValue;
	}
	public String getOptionLabel() {
		return optionLabel;
	}
	public void setOptionLabel(String optionLabel) {
		this.optionLabel = optionLabel;
	}
	public Integer getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}	
}
