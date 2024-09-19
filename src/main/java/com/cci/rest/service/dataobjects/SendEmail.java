package com.cci.rest.service.dataobjects;

public class SendEmail {

	private String managerName;
	private String createdBy;
	private String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	@Override
	public String toString() {
		return "SendEmail [managerName=" + managerName + ", createdBy=" + createdBy + ", errorMessage=" + errorMessage
				+ "]";
	}
}
