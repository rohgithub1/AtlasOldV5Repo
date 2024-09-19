package com.cci.rest.service.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class AtlasReportTypesDAO {

	@Autowired
	private DatabaseService databaseService;
	/*
	 * Service to get dump data for given date range and for given process ID and
	 * for given user
	 */

	public List<Map<String, Object>> reportDumpProcessData(int processID, String startDate, String endDate,
			String userName) {

		List<Map<String, Object>> result = new ArrayList<>();
		try (Connection mssqlconnection = databaseService.getConnection();
				PreparedStatement stmt = mssqlconnection
						.prepareStatement("SELECT ID AS id, process_column_Data FROM revampAtlas_process_data "
								+ "WHERE CREATED_DATE BETWEEN ? AND ? " + "AND PROCESS_ID = ? " + "AND created_by = ? "
								+ "AND IS_ACTIVE = 1 ORDER BY CREATED_DATE ASC")) {
			String startdate = startDate.substring(0, startDate.indexOf('T'));
			String enddate = endDate.substring(0, endDate.indexOf('T'));
			String startdatetime = startdate + " 00:00:00";
			String enddatetime = enddate + " 23:59:59";
			stmt.setString(1, startdatetime);
			stmt.setString(2, enddatetime);
			stmt.setInt(3, processID);
			stmt.setString(4, userName);
			try (ResultSet rs1 = stmt.executeQuery()) {
				while (rs1.next()) {
					LinkedHashMap<String, Object> row = new LinkedHashMap<>();
					JSONObject jsonObj = new JSONObject(rs1.getObject(2).toString());
					Iterator<String> keyList = jsonObj.keys();
					row.put("id", rs1.getObject(1).toString());
					while (keyList.hasNext()) {
						String key = keyList.next();
						String value = jsonObj.getString(key);
						row.put(key, value);
					}
					result.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/*
	 * Service to get dump data for given date range and for given process ID and
	 * for all users
	 */

	public List<Map<String, Object>> reportDumpProcessDataAllUser(int processID, String startDate, String endDate,
			String userName) throws SQLException {
		String startdate = startDate.substring(0, startDate.indexOf('T'));
		startdate = startdate.split("T", 2)[0];
		String enddate = endDate.substring(0, endDate.indexOf('T'));
		enddate = enddate.split("T", 2)[0];
		String startdatetime = startdate + " 00:00:00";
		String enddatetime = enddate + " 23:59:59";
		System.out.println(
				"Process Id and Report Dates---------" + processID + " ->" + startdatetime + ":" + enddatetime);
		List<Map<String, Object>> result = new ArrayList<>();
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs1 = null;

		try {
			mssqlconnection = databaseService.getConnection();
			String query = "SELECT ID AS id, process_column_Data FROM revampAtlas_process_data  WHERE CREATED_DATE BETWEEN ? AND ? AND PROCESS_ID = ? AND IS_ACTIVE = 1 ORDER BY CREATED_DATE ASC";
			pstmt = mssqlconnection.prepareStatement(query);
			pstmt.setString(1, startdatetime);
			pstmt.setString(2, enddatetime);
			pstmt.setInt(3, processID);
			rs1 = pstmt.executeQuery();

			while (rs1.next()) {
				LinkedHashMap<String, Object> row = new LinkedHashMap<>();
				JSONObject jsonObj = new JSONObject(rs1.getObject(2).toString());
				Iterator<String> keyList = jsonObj.keys();
				row.put("id", rs1.getObject(1).toString());
				while (keyList.hasNext()) {
					String key = keyList.next();
					String value = jsonObj.getString(key);
					row.put(key, value);
					System.out.println(key + "!!!" + value);
				}
				result.add(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null) {
					rs1.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/*
	 * Get column name for selected process
	 */
	public List<String> getColumnNameForSelectedProcess(int processId) {

		ArrayList<String> keys = new ArrayList<>();

		String key;
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();

			pstmt = mssqlconnection.prepareStatement(
					"select process_column_Data from revampAtlas_process_data where PROCESS_ID = ? order by 1 desc");

			pstmt.setInt(1, processId);

			rs = pstmt.executeQuery();

			if (rs.next() == false) {
				System.out.println("No result set");
			} else {
				String res = rs.getString(1);

				JSONObject newUserJSON = new JSONObject(res);
				for (Iterator<String> it = newUserJSON.keys(); it.hasNext();) {
					key = it.next();
					keys.add(key);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return keys;
	}

	/*
	 * Activity Report userwise
	 */

	public List<Map<String, Object>> reportActivityDataUserwise(String userName, String startDate, String endDate)
			throws SQLException {
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
			pstmt = mssqlconnection.prepareStatement(
					"select ID as id, ACTIVITY_NAME as activity_name, PROCESS_ID as processId, FORMAT (START_TIME, 'yyyy-MM-dd HH:mm:ss') as startTime, FORMAT (END_TIME, 'yyyy-MM-dd HH:mm:ss')  as endTime, COMMENTS as comments, IS_ACTIVE as isActive, CREATED_BY as createdBy, FORMAT (CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') as createdDate, MODIFIED_BY as modifiedBy, FORMAT (MODIFIED_DATE, 'yyyy-MM-dd HH:mm:ss') as modifiedDate, DURATION as duration from process_data_activity where CREATED_DATE BETWEEN ? and ? and CREATED_BY = ?");
			pstmt.setString(1, startdatetime);
			pstmt.setString(2, enddatetime);
			pstmt.setString(3, userName);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
				}
				result.add(row);
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	/*
	 * Activity Report all user
	 */

	public List<Map<String, Object>> reportActivityDataAllUser(String startDate, String endDate) throws SQLException {
		List<Map<String, Object>> result = new ArrayList<>();
		System.out.println(startDate + ":" + endDate);
		String startdate = startDate.substring(0, startDate.indexOf('T'));
		startdate = startdate.split("T", 2)[0];

		String enddate = endDate.substring(0, endDate.indexOf('T'));
		enddate = enddate.split("T", 2)[0];

		String startdatetime = startdate + " 00:00:00";
		String enddatetime = enddate + " 23:59:59";
		Connection mssqlconnection = databaseService.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = mssqlconnection.prepareStatement(
					"select ID as id, ACTIVITY_NAME as activity_name, PROCESS_ID as processId, FORMAT (START_TIME, 'yyyy-MM-dd HH:mm:ss') as startTime, FORMAT (END_TIME, 'yyyy-MM-dd HH:mm:ss')  as endTime, COMMENTS as comments, IS_ACTIVE as isActive, CREATED_BY as createdBy, FORMAT (CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') as createdDate, MODIFIED_BY as modifiedBy, FORMAT (MODIFIED_DATE, 'yyyy-MM-dd HH:mm:ss') as modifiedDate, DURATION as duration from process_data_activity where CREATED_DATE BETWEEN ? and ?");
			pstmt.setString(1, startdatetime);
			pstmt.setString(2, enddatetime);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
				}
				result.add(row);
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	/*
	 * created- get login logout report for given start date and end date
	 */
	public List<Map<String, Object>> GetLoginLogoutReport(String startDate, String endDate) {

		List<Map<String, Object>> list = new ArrayList<>();
		String startdate = startDate.substring(0, startDate.indexOf('T'));
		startdate = startdate.split("T", 2)[0];

		String enddate = endDate.substring(0, endDate.indexOf('T'));
		enddate = enddate.split("T", 2)[0];

		String startdatetime = startdate + " 00:00:00";
		String enddatetime = enddate + " 23:59:59";

		Connection mssqlconnection = null;

		String selectActivityReportQuery = "select ID as id, ACTIVITY_NAME as activity_name, PROCESS_ID as processId, FORMAT (START_TIME, 'yyyy-MM-dd HH:mm:ss') as startTime, FORMAT (END_TIME, 'yyyy-MM-dd HH:mm:ss')  as endTime, COMMENTS as comments, IS_ACTIVE as isActive, CREATED_BY as createdBy, FORMAT (CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') as createdDate, MODIFIED_BY as modifiedBy, FORMAT (MODIFIED_DATE, 'yyyy-MM-dd HH:mm:ss') as modifiedDate, DURATION as duration";
		selectActivityReportQuery += " from process_data_activity where created_date between '" + startdatetime
				+ "' and '" + enddatetime + "'";
		System.out.println(selectActivityReportQuery);
		Map<String, Object> loginmap = new TreeMap<>();
		Map<String, Object> logoutmap = new HashMap<>();
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			mssqlconnection = databaseService.getConnection();
			PreparedStatement pstmt = mssqlconnection.prepareStatement(selectActivityReportQuery);
			ResultSet rs1 = pstmt.executeQuery();
			while (rs1.next()) {
				String createdBy = rs1.getString("createdBy").toLowerCase();
				String dayDate = rs1.getString("startTime").substring(0, 10);
				String loginKey = createdBy + "-" + dayDate + "Login", logoutKey = createdBy + "-" + dayDate + "Logout";
				if (rs1.getString("activity_name").equalsIgnoreCase("Login")) {
					if (loginmap.containsKey(loginKey)) {
						try {
							String jsonString = loginmap.get(loginKey).toString();
							JSONObject jsonObject = new JSONObject(jsonString);
							String loginTime = jsonObject.getString("LoginTime");
							LocalDateTime dateTime = LocalDateTime.parse(loginTime, formatter);
							LocalDateTime dateTime2 = LocalDateTime.parse(rs1.getString("startTime"), formatter);
							if (dateTime.isAfter(dateTime2)) {
								String formattedDateTime = dateTime2.format(formatter);
								jsonObject.put("LoginTime", formattedDateTime);
								loginmap.put(loginKey, jsonObject.toString());

							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (!loginmap.containsKey(loginKey)) {
						StringBuilder add = new StringBuilder();
						add.append("{");
						add.append("\"Id\": \"").append(rs1.getString("id")).append("\",");
						add.append("\"activity_name\": \"").append(rs1.getString("activity_name")).append("\",");
						add.append("\"LoginTime\": \"").append(rs1.getString("startTime")).append("\",");
						add.append("\"createdBy\": \"").append(createdBy).append("\",");
						add.append("\"isActive\": \"").append(rs1.getString("isActive")).append("\",");
						if (add.charAt(add.length() - 1) == ',') {
							add.deleteCharAt(add.length() - 1);
						}
						add.append("}");
						loginmap.put(loginKey, add.toString());
					}
				}
				if (logoutmap.containsKey(logoutKey)) {
					try {
						String jsonString = logoutmap.get(logoutKey).toString();
						JSONObject jsonObject = new JSONObject(jsonString);
						LocalDateTime dateTime = null;
						if (rs1.getString("endTime") != null) {
							dateTime = LocalDateTime.parse(rs1.getString("endTime"), formatter);
						} else {
							dateTime = LocalDateTime.parse(rs1.getString("startTime"), formatter);
						}
						LocalDateTime dateTime2 = LocalDateTime.parse(jsonObject.get("LogoutTime").toString(),
								formatter);
						if (dateTime2 != null) {
							if (dateTime.isAfter(dateTime2)) {
								String formattedDateTime = dateTime.format(formatter);
								jsonObject.put("LogoutTime", formattedDateTime);
								logoutmap.put(logoutKey, jsonObject.toString());
							}
						} else {
							String formattedDateTime = dateTime.format(formatter);
							jsonObject.put("LogoutTime", formattedDateTime);
							logoutmap.put(logoutKey, jsonObject.toString());
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (!logoutmap.containsKey(logoutKey)) {
					StringBuilder logout = new StringBuilder();
					logout.append("{");
					logout.append("\"Id\": \"").append(rs1.getString("id")).append("\",");
					if (rs1.getString("activity_name").equalsIgnoreCase("Logout")) {
						logout.append("\"activity_name\": \"").append(rs1.getString("activity_name")).append("\",");
					} else {
						logout.append("\"activity_name\": \"").append("Logout(" + rs1.getString("activity_name") + ")")
								.append("\",");
					}
					String endTime = rs1.getString("endTime");
					if (endTime != null && !endTime.isEmpty()) {
						logout.append("\"LogoutTime\": \"").append(rs1.getString("endTime")).append("\",");
					} else {
						logout.append("\"LogoutTime\": \"").append(rs1.getString("startTime")).append("\",");
					}
					logout.append("\"createdBy\": \"").append(createdBy).append("\",");
					logout.append("\"isActive\": \"").append(rs1.getString("isActive")).append("\",");
					if (logout.charAt(logout.length() - 1) == ',') {
						logout.deleteCharAt(logout.length() - 1);
					}
					logout.append("}");
					logoutmap.put(logoutKey, logout.toString());

				}
			}
			loginmap.putAll(logoutmap);
			for (Map.Entry<String, Object> entry : loginmap.entrySet()) {
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put(entry.getKey(), entry.getValue());
				list.add(tempMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Login logout list = " + list);
		return list;
	}

}
