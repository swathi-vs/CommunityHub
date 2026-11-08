package model;

import java.sql.Timestamp;

public class FeatureRecordSummary {

	private Long id;
	private Long formId;
	private Long organizationId;
	private Long tenancyId;
	private Timestamp createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		this.id = v;
	}

	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long v) {
		this.formId = v;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public Long getTenancyId() {
		return tenancyId;
	}

	public void setTenancyId(Long v) {
		this.tenancyId = v;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp v) {
		this.createdAt = v;
	}
}