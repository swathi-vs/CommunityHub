package model;

public class Bed {

	private Long id;

	private String bedLabel;
	private Long roomId;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String v) {
		this.status = v;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBedLabel() {
		return bedLabel;
	}

	public void setBedLabel(String bedLabel) {
		this.bedLabel = bedLabel;
	}
}