package com.cci.rest.service.dataobjects;




public class AtlasActivitiesName {
	 
	private String activityName;
	private int sno;
	private String description;
	
	
	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	public int getSno() {
		return sno;
	}
	public void setSno(int sno) {
		this.sno = sno;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	@Override
	public String toString() {
		return "AtlasActivitiesName [activityName=" + activityName + ", sno=" + sno + ", description=" + description
				+ "]";
	}
	
}
 
