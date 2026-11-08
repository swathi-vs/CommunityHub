package model;

import java.sql.Date;

public class Tenancy {
	private Long id;

	private Long organizationId;

	private Long propertyId;

	private Long roomId;

	private Long bedId;

	private Long userId;

	private Date startDate;

	private Date expectedEndDate;

	private Double depositAmount;

	private String depositStatus;

	private String status;

	private Long createdBy;

	public Long getOrganizationId() {
		return organizationId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		this.id = v;
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

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {

		this.roomId = roomId;
	}

	public Long getBedId() {
		return bedId;
	}

	public void setBedId(Long bedId) {

		this.bedId = bedId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {

		this.userId = userId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {

		this.startDate = startDate;
	}

	public Date getExpectedEndDate() {
		return expectedEndDate;
	}

	public void setExpectedEndDate(Date expectedEndDate) {

		this.expectedEndDate = expectedEndDate;
	}

	public Double getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(Double depositAmount) {

		this.depositAmount = depositAmount;
	}

	public String getDepositStatus() {
		return depositStatus;
	}

	public void setDepositStatus(String depositStatus) {

		this.depositStatus = depositStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {

		this.status = status;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {

		this.createdBy = createdBy;
	}
}