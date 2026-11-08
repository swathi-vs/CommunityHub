package model;

public class FormRelationship {
	private Long id;
	private Long propertyId;
	private Long formId;
	private Boolean isActive;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPropertyId() {
		return propertyId;
	}
	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}
	public Long getFormId() {
		return formId;
	}
	public void setFormId(Long fromId) {
		this.formId = fromId;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
}

