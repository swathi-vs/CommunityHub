package model;

import java.sql.Timestamp;

public class Payment {
	private Long id;
	private Long organizationId;
	private Long invoiceId;
	private Double amount;
	private String method;
	private String reference;
	private Timestamp paidAt;
	private Long recordedUserId;
	private String notes;
	private Timestamp createdAt;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {

		this.organizationId = organizationId;
	}

	public Long getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Long invoiceId) {

		this.invoiceId = invoiceId;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {

		this.amount = amount;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {

		this.method = method;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {

		this.reference = reference;
	}

	public Timestamp getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(Timestamp paidAt) {

		this.paidAt = paidAt;
	}

	public Long getRecordedUserId() {
		return recordedUserId;
	}

	public void setRecordedUserId(Long recordedUserId) {

		this.recordedUserId = recordedUserId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {

		this.notes = notes;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {

		this.createdAt = createdAt;
	}
}