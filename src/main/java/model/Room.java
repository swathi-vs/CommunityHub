package model;

public class Room {

	private Long id;

	private String roomNumber;
	private Long organizationId;
	private Long propertyId;
	private String status;
	private String roomType;
	private Integer capacity;
	private Double monthlyRent;
	private Boolean hasAc;

	public String getStatus() {
		return status;
	}

	public void setStatus(String v) {
		this.status = v;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public Long getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {

		this.roomNumber = roomNumber;
	}

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String v) {
		this.roomType = v;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer v) {
		this.capacity = v;
	}

	public Double getMonthlyRent() {
		return monthlyRent;
	}

	public void setMonthlyRent(Double v) {
		this.monthlyRent = v;
	}

	public Boolean getHasAc() {
		return hasAc;
	}

	public void setHasAc(Boolean v) {
		this.hasAc = v;
	}
}
