package com.cci.rest.service.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.ActiveProcessList;
import com.cci.rest.service.dataobjects.ProcessingDataAtlas;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * 
 * @author Rohit Khimavat
 *
 */

@Component
public class AtlasInsertProcessingDataDao {

	@Autowired
	private DatabaseService databaseService;

	@Autowired
	Atlas_ProcessesAssignedToUser atlasprocessDAO;

	Logger logger = LoggerFactory.getLogger(getClass());
	Date date = new Date();
	DateFormat sdformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/*
	 * Service for add transaction insert data
	 */

	public List<ProcessingDataAtlas> insertDataProcessData(String jsoncolumns)
			throws ParseException, JSONException, SQLException {
		System.out.println("Insert start time : " + sdformat.format(date));
		String removestring = "\"jsonObj\":{\"newState\":";
		String result = jsoncolumns.replace(removestring, "newState :");
		String columnInfo = "selectedProcessColumnName";
		Map<String, String> inputValues = new HashMap<>();
		JSONObject jobj = new JSONObject(result);
		String username = jobj.getString("userName");
		int processId = jobj.getInt("processId");
		String[] key = result.split(columnInfo);

		String columnKey = key[1].replace("\"", "").replace("}", "").replace(":[", "").replace("]", "");
		String[] keyVal1 = columnKey.split(",");
		JSONArray jarray = jobj.getJSONArray("newState");

		for (int i = 0; i < jarray.length(); i++) {
			JSONObject insideJObj = jarray.getJSONObject(i);

			for (String colName : keyVal1) {
				if (insideJObj.has(colName)) {
					String value = insideJObj.getString(colName);
					value = value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("'", "''");
					inputValues.put(colName.trim(), value.trim());
				} else {
				}
			}
		}

		StringBuilder resultBuilder = new StringBuilder();
		for (String colValue : keyVal1) {
			if (inputValues.containsKey(colValue)) {
				resultBuilder.append(",\"").append(colValue).append("\":\"").append(inputValues.get(colValue))
						.append("\"");
			} else {
				inputValues.put(colValue, "\"Blank\"");
				resultBuilder.append(", \"").append(colValue).append("\": \"Blank\"");
			}
		}

		String createdBy = username;
		result = " \"userName\":\"" + createdBy + "\",\"processId\":\"" + processId + "\"" + resultBuilder.toString();
		String modifiedBy = createdBy;
		List<ProcessingDataAtlas> list = new ArrayList<>();
		String starttime = getStartTime(createdBy);
		logger.info("Transaction start time is : " + starttime);
		String endtime = getEndDateTime();
		String duration = getSTartEndDifference(starttime, endtime).toString().trim();
		String createddate = endtime;
		String modifieddate = endtime;

		if (result.contains("~")) {
			result = result.replace('~', ':');
		}

		int isActive = 1;
		StringBuilder jsonColumnBuilder = new StringBuilder("{ \"processId\":\"" + processId + "\", \"startTime\":\""
				+ starttime + "\", \"endTime\":\"" + endtime + "\", \"duration\":\"" + duration + "\", \"isActive\":\""
				+ isActive + "\", \"createdBy\":\"" + createdBy + "\", \"createdDate\":\"" + createddate
				+ "\", \"modifiedBy\":\"" + modifiedBy + "\", \"modifiedDate\":\"" + modifieddate + "\",");
		jsonColumnBuilder.append(result).append("}");

		String jsonColumnData = jsonColumnBuilder.toString();
		System.out.println("jsonColumnData : " + jsonColumnData);
		String insertQuery = "INSERT INTO revampAtlas_process_data (PROCESS_ID, START_TIME, END_TIME, DURATION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, process_column_Data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		System.out.println("insertQuery :" + insertQuery);
		Connection mssqlConnection = null;
		PreparedStatement pstmt = null;
		try {
			mssqlConnection = databaseService.getConnection();
			pstmt = mssqlConnection.prepareStatement(insertQuery);
			pstmt.setInt(1, processId);
			pstmt.setString(2, starttime);
			pstmt.setString(3, endtime);
			pstmt.setString(4, duration);
			pstmt.setInt(5, isActive);
			pstmt.setString(6, createdBy);
			pstmt.setString(7, createddate);
			pstmt.setString(8, modifiedBy);
			pstmt.setString(9, modifieddate);
			pstmt.setString(10, jsonColumnData);
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mssqlConnection != null) {
				mssqlConnection.close();
			}
		}
		System.out.println("Insert end start : " + sdformat.format(date));
		return list;

	}

	public String getEndDateTime() {
		Date today = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		String endDateTime = df.format(today);

		return endDateTime;
	}

	public String getStartTime(String createdBy) throws SQLException, ParseException {
		System.out.println("getStartTime start : " + sdformat.format(date));
		String startdatetime = "";
		Connection mssqlconnection = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		try {
			mssqlconnection = databaseService.getConnection();
			pstmt1 = mssqlconnection.prepareStatement(
					"select END_TIME from revampAtlas_process_data where ID = (select top (1) ID from revampAtlas_process_data where CREATED_BY = ? and END_TIME is not null and IS_Active = 1 order by 1 desc)");
			pstmt2 = mssqlconnection.prepareStatement(

					"SELECT END_TIME FROM PROCESS_DATA_ACTIVITY WHERE ID = (  SELECT TOP(1) ID FROM PROCESS_DATA_ACTIVITY WHERE CREATED_BY = ? AND END_TIME IS NOT NULL AND  IS_ACTIVE = 1 AND  ACTIVITY_NAME NOT IN ( 'PRODUCTION ACTIVITY' ) ORDER BY 1 DESC )");

			pstmt1.setString(1, createdBy);
			rs = pstmt1.executeQuery();
			Date endDateTime = null;
			String LastTransactionEndtime = null;
			while (rs.next()) {
				String endtime = rs.getString(1);
				endDateTime = endDateTime != null ? endDateTime : getFormatedDate(endtime.toString().replace(".0", ""));
				LastTransactionEndtime = LastTransactionEndtime != null ? LastTransactionEndtime
						: getFormatedDateString(endDateTime);
			}
			rs.close();
			if (LastTransactionEndtime == null) {
				pstmt2.setString(1, createdBy);
				rs1 = pstmt2.executeQuery();
				Date activityEndDateTime = null;
				String LastActivityEndtime = null;
				while (rs1.next()) {
					String endtime = rs1.getString(1);
					activityEndDateTime = activityEndDateTime != null ? activityEndDateTime
							: getFormatedDate(endtime.toString().replace(".0", ""));
					LastActivityEndtime = LastActivityEndtime != null ? LastActivityEndtime
							: getFormatedDateString(activityEndDateTime);
				}
				rs1.close();
				endDateTime = activityEndDateTime;
				LastTransactionEndtime = LastActivityEndtime;
			}
			if (endDateTime != null) {
				Date activityEndDateTime = null;
				String LastActivityEndtime = null;
				pstmt2.setString(1, createdBy);
				rs2 = pstmt2.executeQuery();
				while (rs2.next()) {
					String endtime = rs2.getString(1);
					activityEndDateTime = activityEndDateTime != null ? activityEndDateTime
							: getFormatedDate(endtime.toString().replace(".0", ""));
					LastActivityEndtime = LastActivityEndtime != null ? LastActivityEndtime
							: getFormatedDateString(activityEndDateTime);
				}
				rs2.close();
				if (endDateTime.after(activityEndDateTime)) {
					startdatetime = LastTransactionEndtime;
					logger.info("Transaction time as starttime {}", startdatetime);
				} else {
					startdatetime = LastActivityEndtime;
					logger.info("Activity time as starttime {}", startdatetime);
				}
			}

		} catch (SQLException e) {
			logger.error("SQL Exception", e);
			throw e;
		} finally {
			try {
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
				if (pstmt1 != null) {
					pstmt1.close();
				}
				if (pstmt2 != null) {
					pstmt2.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (rs1 != null) {
					rs1.close();
				}
				if (rs2 != null) {
					rs2.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("getStartTime ends : " + sdformat.format(date));
		return startdatetime;
	}

	public Date getFormatedDate(String dateString) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(dateString);
	}

	public String getSTartEndDifference(String starttime, String endtime) {
		String difference = "";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime startdatetime = LocalDateTime.parse(starttime, formatter).atZone(zone);
		ZonedDateTime enddatetime = LocalDateTime.parse(endtime, formatter).atZone(zone);
		Duration diff = Duration.between(startdatetime, enddatetime);
		long hours = diff.toHours();
		diff = diff.minusHours(hours);
		long minutes = diff.toMinutes();
		diff = diff.minusMinutes(minutes);
		long seconds = diff.getSeconds();
		difference = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		return difference;
	}

	public static String estToIst(String dateInput) throws ParseException {
		return changeTimeZone(dateInput, TimeZone.getTimeZone("America/New_York"),
				TimeZone.getTimeZone("Asia/Calcutta"));
	}

	private static String changeTimeZone(String dateInput, TimeZone sourceTimeZone, TimeZone targetTimeZone)
			throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(sourceTimeZone);
		Date date = formatter.parse(dateInput);
		formatter.setTimeZone(targetTimeZone);
		return formatter.format(date);
	}

	public String getFormatedDateString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	/*
	 * ProcessMapping actions new
	 */

	public boolean userProcesMappingActions(String selectedUserName, String userProcessMapping, String userName)
			throws SQLException {

		String startTime = getEndDateTime();
		System.out.println("userProcessMapping : " + userProcessMapping);
		userProcessMapping = userProcessMapping.replace('{', ' ');
		userProcessMapping = userProcessMapping.replace('[', ' ');
		userProcessMapping = userProcessMapping.replace('}', ' ');
		userProcessMapping = userProcessMapping.replace(']', ' ');
		userProcessMapping = userProcessMapping.replace('\"', ' ');
		System.out.println("userProcessMapping : " + userProcessMapping);
		boolean result = false;

		String domainNameOfSelectedUser = selectedUserName;
		System.out.println("domainNameOfSelectedUser : " + domainNameOfSelectedUser);

		Map<String, String> inputValues = new HashMap<>();

		String[] userProcessMappingData = userProcessMapping.split(",");

		for (int i = 0; i < userProcessMappingData.length; i++) {
			String[] sessionDataAaray = userProcessMappingData[i].split(":");

			String key = sessionDataAaray[0].replace('{', ' ');

			key = key.replace('"', ' ');

			if (key.contains("[")) {
				key = key.replace('[', ' ');
			}

			String value = sessionDataAaray[1].replace('"', ' ');

			value = value.replace('}', ' ');

			if (value.contains("]")) {
				value = value.replace(']', ' ');
			}

			inputValues.put(key.trim(), value.trim());

		}
//		System.out.println("Get set ()" + inputValues.entrySet());

		String processMappingSelectQuery = "select ID from User_Process_Mapping where USERNAME = '"
				+ domainNameOfSelectedUser + "' and IS_ACTIVE = 1";
		String processMappingUpdateQuery = "update User_Process_Mapping set USERNAME = '" + domainNameOfSelectedUser
				+ "', MODIFIED_BY = '" + userName + "', MODIFIED_DATE = '" + startTime + "', MAPPING_DATA = '{";
		String colValues = "";
		String processMappingInsertQuery = "insert into User_Process_Mapping (USERNAME, MAPPING_DATA, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, IS_ACTIVE) values ('"
				+ domainNameOfSelectedUser + "',";
		int ID;
		String keyName = null;
		String keyValue = null;

		for (Map.Entry<String, String> set : inputValues.entrySet()) {
			keyName = set.getKey();
			if (keyName.length() > 0) {
				keyValue = inputValues.get(keyName);

				colValues = colValues + "\"" + keyName + "\":\"" + keyValue + "\",";
			}

//			colValues = colValues + "}";
//			System.out.println(colValues);

			colValues = colValues.replace(",}", "}");
//			System.out.println(colValues);

			colValues = colValues.toString();
//			System.out.println(colValues);

			colValues = colValues.replace("}", "");
//			System.out.println(colValues);

			colValues = colValues.toString();

		}

		colValues = colValues + '}';
		colValues = colValues.replace(",}", "}");
//		System.out.println(colValues);

		colValues = colValues.replace('}', ' ');
		Connection mssqlconnection = databaseService.getConnection();
		Statement stmt = null;
		try {

			stmt = mssqlconnection.createStatement();

			ResultSet rs = stmt.executeQuery(processMappingSelectQuery);

			if (rs.next()) {
				ID = Integer.parseInt(rs.getString("ID"));

				processMappingUpdateQuery = processMappingUpdateQuery + colValues + "}' where ID = " + ID;
//				System.out.println("processMappingUpdateQuery : " + processMappingUpdateQuery);
				rs.close();

				stmt.execute(processMappingUpdateQuery);
				result = true;
			} else {
				processMappingInsertQuery = processMappingInsertQuery + "'{" + colValues + "}', '" + userName + "', '"
						+ startTime + "', '" + userName + "','" + startTime + "',1)";
//				System.out.println("processMappingInsertQuery : " + processMappingInsertQuery);

				stmt.execute(processMappingInsertQuery);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (mssqlconnection != null) {
				mssqlconnection.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}

		return result;
	}

	/*
	 * ProcessMapping actions
	 */

	public boolean procesMappingActions(String processMappingData) throws SQLException {
		String startTime = getEndDateTime();

		boolean result = false;
		int processActivityMetadataId = 0;
		String removeString = "body";
		String activeProcess = processMappingData.replace(removeString, "");
//		System.out.println(activeProcess);
		Map<String, String> inputValues = new HashMap<>();

		String[] processResultData = activeProcess.split(",");
		int k = 0;

		for (int i = 0; i < processResultData.length; i++) {
			String[] sessionDataAaray = processResultData[i].split(":");

			String key = sessionDataAaray[0].replace('{', ' ');
			key = key.replace('"', ' ');

			String value = sessionDataAaray[1].replace('"', ' ');
			if (value.contains("[")) {
				value = value.replace("[", "");
			}
			if (value.contains("]")) {
				value = value.replace("]", "");
			}
			value = value.replace('}', ' ');
			inputValues.put(key.trim(), value.trim());
		}

		int userRoleId;
		int userId;
		String selectedUser = (String) inputValues.get("selectedUser");
		System.out.println(selectedUser);
		String processName = (String) inputValues.get("processName");
		String userName = (String) inputValues.get("userName");
//		System.out.println("process Name " + processName);

		String domainNameOfSelectedUser = selectedUser;
//		System.out.println("domainNameOfSelectedUser : " + domainNameOfSelectedUser);

		String[] processList = processName.split("~");
		Connection mssqlconnection = null;
		Statement stmt = null;
		for (int i = 0; i < processList.length; i++) {

			try {
				mssqlconnection = databaseService.getConnection();
				stmt = mssqlconnection.createStatement();

				String getUserIDQuery = "select ID from users where DOMAIN_LOGIN_NAME = '" + domainNameOfSelectedUser
						+ "' and IS_ACTIVE = 1";

				ResultSet rs0 = stmt.executeQuery(getUserIDQuery);

				if (rs0.next()) {
					userId = Integer.parseInt(rs0.getString("ID").toString());

					String getUserRoleIDQuery = "select ID from user_role_mappings where USER_ID = " + userId
							+ " and IS_ACTIVE = 1";

					ResultSet rs = stmt.executeQuery(getUserRoleIDQuery);
					if (rs.next()) {
						userRoleId = Integer.parseInt(rs.getString("ID").toString());
						System.out.println("userID & userRoleId" + userId + " " + userRoleId);

						String selectProcessIdQuery = "select ID from processes where process = '" + processList[i]
								+ "' and IS_ACTIVE = 1";
						int processId;
						String newProcessName = "";
						rs0.close();
						ResultSet rs1 = stmt.executeQuery(selectProcessIdQuery);

						if (rs1.next()) {
							processId = Integer.parseInt(rs1.getString("ID").toString());
							newProcessName = processList[i] + "-Processing";
							rs1.close();
							System.out.println("processId & processName : " + processId + " " + newProcessName);

							String checkProcessIDQuery = "select * from process_activity_mappings where process_id = "
									+ processId + " and IS_ACTIVE = 1";
							ResultSet rs2 = stmt.executeQuery(checkProcessIDQuery);

							if (rs2.next()) {
								System.out.println(
										"The process entry already exists in the process_activity_mappings table.");
								String getProcessActivityMappingIdQuery = "select top 1 id from process_activity_mappings where process_id = "
										+ processId + " and IS_ACTIVE = 1";
								rs2.close();
								ResultSet rs3 = stmt.executeQuery(getProcessActivityMappingIdQuery);
								int processactivityId;
								if (rs3.next()) {
									processactivityId = Integer.parseInt(rs3.getString("ID").toString());
									System.out.println("processactivityId : " + processactivityId);
									rs3.close();

									String getProcessActivityMetadataIdQuery = "select top 1 id from process_activity_metadata where PROCESS_ACTIVITY_ID = "
											+ processactivityId + " and IS_ACTIVE = 1";
									ResultSet rs4 = stmt.executeQuery(getProcessActivityMetadataIdQuery);

									if (rs4.next()) {
										processActivityMetadataId = Integer.parseInt(rs4.getString("ID").toString());
										System.out.println("processActivityMetadataId : " + processActivityMetadataId);
										rs4.close();
									} else {
										String insertProcessActivityMetadataQuery = "insert into process_activity_metadata (PROCESS_ACTIVITY_ID, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE) values ("
												+ processactivityId + ", '" + newProcessName + "', 1, '" + userName
												+ "', '" + startTime + "', '" + userName + "', '" + startTime + "')";
										System.out.println("insertProcessActivityMetadataQuery : "
												+ insertProcessActivityMetadataQuery);

										stmt.execute(insertProcessActivityMetadataQuery);
										getProcessActivityMetadataIdQuery = "select top 1 id from process_activity_metadata where PROCESS_ACTIVITY_ID = "
												+ processactivityId + " and IS_ACTIVE = 1";
										ResultSet rs5 = stmt.executeQuery(getProcessActivityMetadataIdQuery);
										if (rs5.next()) {
											processActivityMetadataId = Integer
													.parseInt(rs5.getString("ID").toString());
										}
									}
								}
							} else {
								String insertProcessActivityMappingQuery = "insert into process_activity_mappings (PROCESS_ID, ACTIVITY_ID, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE) VALUES ("
										+ processId + ", 1, '" + newProcessName + "', 1, '" + userName + "', '"
										+ startTime + "', '" + userName + "', '" + startTime + "')";
								System.out.println(
										"insertProcessActivityMappingQuery : " + insertProcessActivityMappingQuery);
								stmt.execute(insertProcessActivityMappingQuery);

								String getProcessActivityMappingIdQuery = "select top 1 id from process_activity_mappings where process_id = "
										+ processId;
								ResultSet rs3 = stmt.executeQuery(getProcessActivityMappingIdQuery);
								int processactivityId;
								if (rs3.next()) {
									processactivityId = Integer.parseInt(rs3.getString("ID").toString());
									rs3.close();

									String insertProcessActivityMetadataQuery = "insert into process_activity_metadata (PROCESS_ACTIVITY_ID, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE) values ("
											+ processactivityId + ", '" + newProcessName + "', 1, '" + userName + "', '"
											+ startTime + "', '" + userName + "', '" + startTime + "')";
									System.out.println("insertProcessActivityMetadataQuery : "
											+ insertProcessActivityMetadataQuery);

									stmt.execute(insertProcessActivityMetadataQuery);

									String getProcessActivityMetadataIdQuery = "select top 1 id from process_activity_metadata where PROCESS_ACTIVITY_ID = "
											+ processactivityId + " and IS_ACTIVE = 1";
									ResultSet rs4 = stmt.executeQuery(getProcessActivityMetadataIdQuery);

									if (rs4.next()) {
										processActivityMetadataId = Integer.parseInt(rs4.getString("ID").toString());
										rs4.close();
									}
								}
							}
							String descriptionPamt = domainNameOfSelectedUser + "-Processing-" + newProcessName;
							System.out.println("descriptionPamt & processActivityMetadataId : " + descriptionPamt + " "
									+ processActivityMetadataId);

							String verifyMappingData = "select ID from mappings where USER_ROLE_ID = " + userRoleId
									+ " and PROCESS_ACTIVITY_METADATA_ID = " + processActivityMetadataId
									+ " and IS_ACTIVE = 1";
							System.out.println("verifyMappingData : " + verifyMappingData);
							ResultSet rs5 = stmt.executeQuery(verifyMappingData);
							if (rs5.next()) {
								result = false;
							} else {
								String insertMappingQuery = "insert into mappings (USER_ROLE_ID, PROCESS_ACTIVITY_METADATA_ID, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE) values ("
										+ userRoleId + ", " + processActivityMetadataId + ", '" + descriptionPamt
										+ "', 1, '" + userName + "', '" + startTime + "', '" + userName + "', '"
										+ startTime + "') ";
								System.out.println("insertMappingQuery : " + insertMappingQuery);

								stmt.execute(insertMappingQuery);
							}
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
				result = false;
			} finally {
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			}
		}

		return result;

	}

	/*
	 * Add new process and update process
	 */

	public boolean insertNewProcess(String dynamicColumns) {
		String startTime = getEndDateTime();

		boolean result = false;
		int is_active = 1;
		boolean isAdd = false;

		String removestring = "\"dynamicProcessColumns\"";

		String jsonresulr = dynamicColumns.replace(removestring, "");
		jsonresulr = jsonresulr.replace(":[", "");

		jsonresulr = jsonresulr.replace(":\"{", "{");

		jsonresulr = jsonresulr.replace("],", ",");

		jsonresulr = jsonresulr.replace("{\\\"\\\"", "");

		jsonresulr = jsonresulr.replace("\"description\"", "\"description\":");

//		jsonresulr = "{\"processName\":\"sss\",\"userName\":\"rkhimava\",\"description\"::\"test12344\",{\"id\":1,\"testCol1\":\"null\"},{\"id\":2,\"testCol2\":\"test1~test2~test3\"},\"isAdd\":\"true\",\"isActive\":\"true\"}";
		jsonresulr = jsonresulr.replace("\"description\"::", "\"description\":");

		LinkedHashMap<String, String> inputValues = new LinkedHashMap<>();

		String[] insertResultData = jsonresulr.split(",");
		int k = 0;
		int j = 0;
		for (int i = 0; i < insertResultData.length; i++) {
			String[] sessionDataAaray = insertResultData[i].split(":");

			String key = sessionDataAaray[0].replace('{', ' ');
			key = key.replace('"', ' ');

			String value = sessionDataAaray[1].replace('"', ' ');

			value = value.replace('}', ' ');

			inputValues.put(key.trim(), value.trim());
		}

		String userName = (String) inputValues.get("userName");
		String processName = (String) inputValues.get("processName");
		String description = (String) inputValues.get("description");
		String type = (String) inputValues.get("isAdd");
		String softDelete = (String) inputValues.get("isActive");

		if (softDelete.equalsIgnoreCase("false")) {
			try {
				Connection mssqlconnection = databaseService.getConnection();
				Statement stmt = mssqlconnection.createStatement();

				String auditdelete_query = "insert into Atlas_Audit_data (ACTIVITY_NAME, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, OLD_VALUE, NEW_VALUE) values ('"
						+ processName + "', 'deleting the process', '1','" + userName + "', '" + startTime + "', '"
						+ userName + "', '" + startTime + "',(select Process_Columns from processes where PROCESS = '"
						+ processName + "' and IS_ACTIVE = 1), 'no data')";

				String query = "update processes set IS_ACTIVE = 0 , MODIFIED_BY = '" + userName
						+ "' , MODIFIED_DATE = '" + startTime + "' where process = '" + processName + "'";
				System.out.println("query for new process: " + query);
				stmt.execute(auditdelete_query);
				stmt.execute(query);

				int count = stmt.getUpdateCount();

				if (count == 1) {
					System.out.println("deleted process successfully");
					result = true;
				}
				mssqlconnection.close();
			} catch (Exception e) {
				e.printStackTrace();
				result = false;
			}
		} else {
			if (type.equalsIgnoreCase("true")) {
				isAdd = true;
			}

			List<ProcessingDataAtlas> list = new ArrayList<>();

			String jsonColumn = "{ \"PROCESS\":\"" + processName + "\", \"DESCRIPTION\":\"" + description
					+ "\", \"IS_ACTIVE\":\"" + is_active + "\", \"createdBy\":\"" + userName + "\", \"CREATED_DATE\":\""
					+ startTime + "\", \"MODIFIED_BY\":\"" + userName + "\", \"MODIFIED_DATE\":\"" + startTime + "\",";

			String keyName = null;
			String keyValue = null;
			for (Map.Entry<String, String> set : inputValues.entrySet()) {
				if (set.getKey().equals("processName") || set.getKey().equals("userName")
						|| set.getKey().equals("description") || set.getKey().equals("isAdd")
						|| set.getKey().equals("id") || set.getKey().equals("isActive")) {

				} else {
					keyName = set.getKey();
					if (keyName.length() > 0) {
						keyValue = inputValues.get(keyName);

						jsonColumn = jsonColumn + "\"" + keyName + "\":\"" + keyValue + "\",";
					}

				}
			}
			jsonColumn = jsonColumn + "}";

			jsonColumn = jsonColumn.replace(",}", "}");

			jsonColumn = jsonColumn.toString();

			jsonColumn = jsonColumn.replace("}", "");

			jsonColumn = jsonColumn.toString();

			System.out.println("jsoncolumn - " + jsonColumn);

			try {
				Connection mssqlconnection = databaseService.getConnection();
				Statement stmt = mssqlconnection.createStatement();

				String processNameExistsQuery = "select process from processes where process = '" + processName
						+ "' and IS_ACTIVE = 1";
				System.out.println("processNameExistsQuery : " + processNameExistsQuery);
				ResultSet rs = stmt.executeQuery(processNameExistsQuery);
				int processExists = 0;

				String query = "";
				if (rs.next() && isAdd) {
					result = false;
					processExists = processExists + 1;
				} else if (isAdd) {   
					query = "insert into processes (PROCESS, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, Process_Columns) values ";
					query = query + "( '" + processName + "','" + description + "'," + is_active + ",'" + userName
							+ "','" + startTime + "','" + userName + "','" + startTime + "'";
					query = query + ",'" + jsonColumn + "}' )";

					String auditAdd_query = "insert into Atlas_Audit_data (ACTIVITY_NAME, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, OLD_VALUE, NEW_VALUE) values ('"
							+ processName + "', 'added the process', '1','" + userName + "', '" + startTime + "', '"
							+ userName + "', '" + startTime
							+ "','No previous entry present', (select Process_Columns from processes where PROCESS = '"
							+ processName + "' and IS_ACTIVE = 1))";

					System.out.println("query for new process: " + query);
					stmt.execute(query);
					System.out.println("audit query after addition of the new process. " + auditAdd_query);
					stmt.execute(auditAdd_query);
					result = true;
				} else {
					String oldData = "";
					String m_query = "select Process_Columns from processes where PROCESS = '" + processName
							+ "' and IS_ACTIVE = 1";
					System.out.println(m_query);
					ResultSet oldrs = stmt.executeQuery("select Process_Columns from processes where PROCESS = '"
							+ processName + "' and IS_ACTIVE = 1");
					
					while (oldrs.next()) {
						
						oldData = oldrs.getString("Process_Columns").toString();
						System.out.println("oldData : " + oldData);	
						
					}

					oldData = oldData.replace("{", "");
					oldData = oldData.replace("}", "");
					oldData = "'" + oldData + "'";
					System.out.println("oldData : " + oldData);

					query = "update processes set Process_Columns = ";
					query = query + "'{" + jsonColumn + "}'";
					query = query + ", MODIFIED_BY = '" + userName + "' , MODIFIED_DATE = '" + startTime
							+ "' , DESCRIPTION = '" + description + "' where PROCESS = '" + processName + "'";

					System.out.println("query for new process updation: " + query);
					stmt.execute(query);
					String auditUpdate_query = "insert into Atlas_Audit_data (ACTIVITY_NAME, DESCRIPTION, IS_ACTIVE, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, OLD_VALUE, NEW_VALUE) values ('"
							+ processName + "', 'updated " + processName + " process', '1','" + userName + "', '"
							+ startTime + "', '" + userName + "', '" + startTime + "'," + oldData
							+ ", (select Process_Columns from processes where PROCESS = '" + processName
							+ "' and IS_ACTIVE = 1))";
					System.out.println("query for entry within audit table for updation : " + auditUpdate_query);
					stmt.execute(auditUpdate_query);
					result = true;
				}

				int count = stmt.getUpdateCount();

				if (count == 1 && processExists == 0) {
					System.out.println("Inserted new process successfully");
					result = true;
				} else if (isAdd && processExists > 0) {
					System.out.println("The process already exists and cannot be added");
					result = false;
				} else {
					System.out.println("The process cannot be updated, please check.");
					result = false;
				}
				mssqlconnection.close();
			} catch (Exception e) {
				e.printStackTrace();
				result = false;
			}
		}

		return result;
	}

	/*
	 * --created
	 * 
	 */
	public List<Map<String, Object>> getRevampProcessDataActivityObject(String startDate, String endDate) {
		List<Map<String, Object>> result = new ArrayList<>();

		System.out.println(startDate + ":" + endDate);
		String startdate = startDate.substring(0, startDate.indexOf('T'));
		startdate = startdate.split("T", 2)[0];

		String enddate = endDate.substring(0, endDate.indexOf('T'));
		enddate = enddate.split("T", 2)[0];

		String startdatetime = startdate + " 00:00:00";
		String enddatetime = enddate + " 23:59:59";
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();
//			pstmt = mssqlconnection.prepareStatement(
//					"select  ID as Id , PROCESS_ID as processId,Created_BY as created_By, FORMAT (CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') as createdDate,DURATION as duration,process_column_Data as process_column_data,IS_ACTIVE as is_active"
//							+ " from revampAtlas_process_data where CREATED_DATE BETWEEN ? and ?  AND IS_ACTIVE =1 order by  createdDate ");
			pstmt=mssqlconnection.prepareStatement("SELECT r.ID AS Id, r.PROCESS_ID as processId, "
					+ " p.PROCESS AS Process, "
					+ " r.Created_BY AS created_By, "
					+ " FORMAT(r.CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') AS createdDate, "
					+ " r.DURATION AS duration, "
					+ " r.process_column_Data AS process_column_data, "
					+ " r.IS_ACTIVE AS is_active "
					+ "FROM revampAtlas_process_data r "
					+ "JOIN Processes p ON r.PROCESS_ID = p.ID "
					+ "WHERE r.CREATED_DATE BETWEEN ? AND ? "
					+ "  AND r.IS_ACTIVE = 1 "
					+ "ORDER BY createdDate;");
			pstmt.setString(1, startdatetime);
			pstmt.setString(2, enddatetime);
			System.out.println(" " + pstmt.toString());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();

				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
				}
				result.add(row);
//				System.out.println(" " + result.toString());
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return result;

	}

}
