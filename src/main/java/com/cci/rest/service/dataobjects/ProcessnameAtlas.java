package com.cci.rest.service.dataobjects;


public class ProcessnameAtlas {

private String processName;
private int processId;
private String description;
private String Process_Columns;
private String userName;

public String getProcessName() {
	return processName;
}


public void setProcessName(String processName) {
	this.processName = processName;
}


public int getProcessId() {
	return processId;
}


public void setProcessId(int processId) {
	this.processId = processId;
}


public String getDescription() {
	return description;
}


public void setDescription(String description) {
	this.description = description;
}


public String getProcess_Columns() {
	return Process_Columns;
}


public void setProcess_Columns(String process_Columns) {
	Process_Columns = process_Columns;
}


public String getUserName() {
	return userName;
}


public void setUserName(String userName) {
	this.userName = userName;
}


@Override
public String toString() {
	return "ProcessnameAtlas [processName=" + processName + ", processId=" + processId + ", description=" + description
			+ ", Process_Columns=" + Process_Columns + ", userName=" + userName + "]";
}







}
