package com.cci.rest.service.dataobjects;

import java.sql.Date;
import java.util.ArrayList;

public class Atlas_ActionActivities {

	private String activity;
	private String description;
	private int isActive;
	private String createdBy;
	private Date createdDate;
	private String modifiedBy;
	private Date modifiedDate;
	
	
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getIsActive() {
		return isActive;
	}
	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	

	
	@Override
	public String toString() {
		return "Activity [activity=" + activity + ",description=" + description + ",isActive=" + isActive + ",createdBy=" + createdBy + ",createdDate=" + createdDate + ", modifiedDate=" + modifiedDate + " ]";
	}
}
