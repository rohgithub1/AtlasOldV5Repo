package com.cci.rest.service.dataobjects;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AtlasUsers {

	private String domainLoginName;
	private int processID;
	private String firstName;
	private String lastName;
	private String description;
	private String createdBy;
	private Date createdDate;
	private String modifiedBy;
	private Date modifiedDate;
	private Date lastLogin;
	private int userId;
	private int roleId;
	private String roleName;
	private int userRoleId;	
	private boolean isAutheticateUser;
	private ArrayList<Object> mappingIdList;
	private HashMap<Object, Object> processMap;
	private HashMap<Object, Object> ActivityMap;
	private HashMap<Object, Object> processActivityMap;
	private HashMap<String, String> activeUsersList;
	
	public String getDomainLoginName() {
		return domainLoginName;
	}
	public void setDomainLoginName(String domainLoginName) {
		this.domainLoginName = domainLoginName;
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
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	
	public boolean isAutheticateUser() {
		return isAutheticateUser;
	}
	public void setAutheticateUser(boolean isAutheticateUser) {
		this.isAutheticateUser = isAutheticateUser;
	}
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public int getUserRoleId() {
		return userRoleId;
	}
	public void setUserRoleId(int userRoleId) {
		this.userRoleId = userRoleId;
	}
	
	public ArrayList<Object> getMappingIdList() {
		return mappingIdList;
	}
	public void setMappingIdList(ArrayList<Object> mappingIdList) {
		this.mappingIdList = mappingIdList;
	}
	public HashMap<Object, Object> getProcessMap() {
		return processMap;
	}
	public void setProcessMap(HashMap<Object, Object> processMap) {
		this.processMap = processMap;
	}
	public HashMap<Object, Object> getActivityMap() {
		return ActivityMap;
	}
	public void setActivityMap(HashMap<Object, Object> activityMap) {
		ActivityMap = activityMap;
	}
	public HashMap<Object, Object> getProcessActivityMap() {
		return processActivityMap;
	}
	public void setProcessActivityMap(HashMap<Object, Object> processActivityMap) {
		this.processActivityMap = processActivityMap;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getProcessID() {
		return processID;
	}
	public void setProcessID(int processID) {
		this.processID = processID;
	}
	public HashMap<String, String> getActiveUsersList() {
		return activeUsersList;
	}
	public void setActiveUsersList(HashMap<String, String> activeUsersList) {
		this.activeUsersList = activeUsersList;
	}
	
	@Override
	public String toString() {
		return "AtlasUsers [domainLoginName=" + domainLoginName + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", description=" + description + ", createdBy=" + createdBy + ", createdDate=" + createdDate
				+ ", modifiedBy=" + modifiedBy + ", modifiedDate=" + modifiedDate + ", lastLogin=" + lastLogin
				+ ", userId=" + userId + ", roleId=" + roleId + ", roleName=" + roleName + ", userRoleId=" + userRoleId
				+ ", isAutheticateUser=" + isAutheticateUser + ", mappingIdList=" + mappingIdList + ", processMap="
				+ processMap + ", ActivityMap=" + ActivityMap + ", processActivityMap=" + processActivityMap + ", processID=" + processID + ",activeUsersList="+activeUsersList+"]";
	}
	
}
