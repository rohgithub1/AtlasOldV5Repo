package com.cci.rest.service.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.ActiveProcessList;
import com.cci.rest.service.dataobjects.ProcessnameAtlas;

@Component
public class AtlasGetProcessDataDAO {

	@Autowired
	private DatabaseService databaseService;

	public List<Map<String, Object>> getProcessDataAsJSON(int processID, int limit) throws SQLException {
		List<Map<String, Object>> result = new ArrayList<>();
		if (limit < 1) {
			limit = 10;
		}
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> columnNames = getColumnNameForSelectedProcess(processID);
		try {
			mssqlconnection = databaseService.getConnection();
			pstmt = mssqlconnection.prepareStatement("SELECT TOP (?) ID AS id, PROCESS_ID AS processId, START_TIME AS startTime, END_TIME AS endTime, duration, IS_ACTIVE AS isActive, CREATED_BY AS createdBy, CREATED_DATE AS createdDate, MODIFIED_BY AS modifiedBy, MODIFIED_DATE AS modifiedDate, process_column_Data FROM revampAtlas_process_data WHERE PROCESS_ID = ? AND IS_ACTIVE = 1 ORDER BY CREATED_DATE DESC");
			pstmt.setInt(1, limit);
			pstmt.setInt(2, processID);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, Object> row = new LinkedHashMap<>();
				String jsonColumnData = rs.getString("process_column_Data");
				JSONObject jsonNode = new JSONObject(jsonColumnData);
				row.put("id", rs.getString("id"));
				row.put("processId", rs.getInt("processId"));
				row.put("startTime", rs.getString("startTime"));
				row.put("endTime", rs.getString("endTime"));
				row.put("duration", rs.getString("duration"));
				row.put("isActive", rs.getInt("isActive"));
				row.put("createdBy", rs.getString("createdBy"));
				row.put("createdDate", rs.getString("createdDate"));
				row.put("modifiedBy", rs.getString("modifiedBy"));
				row.put("modifiedDate", rs.getString("modifiedDate"));
				row.put("userName", rs.getString("createdBy"));
				
				for (String key : columnNames) {
					String value = jsonNode.getString(key);
					row.put(key, value);
				}
				result.add(row);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/*
	 * Get New data method
	 */

	public List<Map<String, Object>> getNewProcessDataAsJson(String jsonColumns)
			throws ParseException, JSONException, SQLException, IOException {
		JSONObject jobj = new JSONObject(jsonColumns);
		JSONArray jarray = jobj.getJSONArray("selectedProcessColumnName");
		int processId = jobj.getInt("processId");
		List<String> list = new ArrayList<>(jarray.length());
		List<Map<String, Object>> result = new ArrayList<>();
		for (int i = 0; i < jarray.length(); i++) {
			list.add(jarray.getString(i));
		}
		if (list.isEmpty()) {
			list = getColumnNameForSelectedProcess(processId);
		}
		Connection mssqlConnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		try  {
			mssqlConnection = databaseService.getConnection();
			pstmt = mssqlConnection.prepareStatement("select top 10 ID as id, PROCESS_ID as processId, START_TIME as startTime, END_TIME as endTime, duration, IS_ACTIVE as isActive, CREATED_BY as createdBy, CREATED_DATE as createdDate, MODIFIED_BY as modifiedBy, MODIFIED_DATE as modifiedDate, process_column_Data FROM revampAtlas_process_data WHERE PROCESS_ID = ? and IS_ACTIVE = 1 ORDER BY CREATED_DATE DESC");
			pstmt.setInt(1, processId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, Object> row = new LinkedHashMap<>();
				String jsonColumnData = rs.getString("process_column_Data");
				JSONObject jsonNode = new JSONObject(jsonColumnData);
				row.put("id", rs.getString("id"));
				row.put("processId", rs.getInt("processId"));
				row.put("startTime", rs.getString("startTime"));
				row.put("endTime", rs.getString("endTime"));
				row.put("duration", rs.getString("duration"));
				row.put("isActive", rs.getInt("isActive"));
				row.put("createdBy", rs.getString("createdBy"));
				row.put("createdDate", rs.getString("createdDate"));
				row.put("modifiedBy", rs.getString("modifiedBy"));
				row.put("modifiedDate", rs.getString("modifiedDate"));
				row.put("userName", rs.getString("createdBy"));
				for (String key : list) {
					String value = jsonNode.getString(key);
					row.put(key, value);
				}
				result.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Failed to fetch process data", e);
		}finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlConnection != null) {
					mssqlConnection.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/*
	 * Get Review data for selected user
	 */

	public List<Map<String, Object>> getProcessReviewDataForUser(int processID, String userName) throws SQLException {

		List<Map<String, Object>> result = new ArrayList<>();
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();
			AtlasUpdateProcessingDataDao aupd = new AtlasUpdateProcessingDataDao();
			String startDate = aupd.getDate();
			String endDate = startDate;
			startDate = startDate+" 00:00:00";
			System.out.println("Review - startDate : "+startDate);
			endDate = endDate+" 23:59:59";
			System.out.println("Review - endDate : "+endDate);
//			List<String> list = getColumnNameForSelectedProcess(processID);
			String query = "select ID as id, process_column_Data from revampAtlas_process_data where PROCESS_ID = ? and created_By = ? and CREATED_DATE between ? and ? and IS_ACTIVE = 1 order by ID desc";	
			pstmt = mssqlconnection.prepareStatement(query);
			pstmt.setInt(1, processID);
			pstmt.setString(2, userName);
			pstmt.setString(3, startDate);
			pstmt.setString(4, endDate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, Object> row = new LinkedHashMap<>();
				JSONObject jsonObj = new JSONObject(rs.getObject(2).toString());
				Iterator keyList = jsonObj.keys();
				row.put("id", rs.getObject(1).toString());
				while (keyList.hasNext()) {
					String key = keyList.next().toString();
					String value = jsonObj.getString(key);
					row.put(key, value);
				}
				result.add(row);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/*
	 * Get all processes which are active
	 */

	public List<ActiveProcessList> getAllActiveProcesses() {
	    List<ActiveProcessList> list = new ArrayList<>();
	    Connection mssqlconnection = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    try {
	        mssqlconnection = databaseService.getConnection();
	        pstmt=mssqlconnection.prepareStatement("select ID, PROCESS, DESCRIPTION, Process_Columns from processes where IS_ACTIVE = 1");
	        rs = pstmt.executeQuery();
	        while (rs.next()) {
	            ActiveProcessList processnameAtlas = new ActiveProcessList();
	            processnameAtlas.setProcessId(rs.getInt("ID"));
	            processnameAtlas.setProcessName(rs.getString("PROCESS"));
	            processnameAtlas.setDescription(rs.getString("DESCRIPTION"));
	            String processColumns = rs.getString("Process_Columns");
	    
	            if (processColumns != null) {
	            	String splitpart = rs.getString("Process_Columns").toString();
//					System.out.println("Process_Columns" + splitpart);
					splitpart = splitpart.replace("{", "");
					splitpart = splitpart.replace("}", "");

					String newCol[] = splitpart.split(",");
					String finalColumn = "";
					int m = 1;
					boolean isAdd = true;
					for (int i = 0; i < newCol.length; i++) {
//						System.out.println("newCol[i] : "+ newCol[i]);
						String[] coldata = newCol[i].split(":");

						if (coldata[0].toString().trim().equalsIgnoreCase("\"PROCESS\"")
								|| coldata[0].toString().trim().equalsIgnoreCase("\"DESCRIPTION\"")
								|| coldata[0].toString().trim().equalsIgnoreCase("\"IS_ACTIVE\"")
								|| coldata[0].toString().trim().equalsIgnoreCase("\"createdBy\"")
								|| coldata[0].toString().trim().equalsIgnoreCase("\"CREATED_BY\"")
								|| coldata[0].toString().trim().equalsIgnoreCase("\"CREATED_DATE\"")
								|| coldata[0].toString().trim().equalsIgnoreCase("\"MODIFIED_BY\"")
								|| coldata[0].toString().trim().equalsIgnoreCase("\"MODIFIED_DATE\"")) {
//							System.out.println("unwanted : "+coldata[0]);
							isAdd = false;
						} else {
							isAdd = true;
//							System.out.println(coldata[0]);
							if (coldata.length > 1) {
								newCol[i] = "{\"id\":" + m + ", \"col\":" + coldata[0] + ", \"dd\":" + coldata[1]
										+ " }";
							} else {
								newCol[i] = "{\"id\":" + m + ", \"col\":" + coldata[0] + ", \"dd\":" + null + " }";
							}

						}

						if (isAdd) {
//							System.out.println(newCol[i]);
							if (i == newCol.length - 1) {
								finalColumn = finalColumn + newCol[i] + "";
							} else {
								finalColumn = finalColumn + newCol[i] + ",";
							}
							m = m + 1;
						}
					}
					finalColumn = finalColumn.replace('~', ',');

//					System.out.println(finalColumn);

					processnameAtlas.setProcess_Columns(finalColumn.toString());
	            }
	            list.add(processnameAtlas);
	        }
	    } catch (SQLException e) {
	       e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            if (mssqlconnection != null) mssqlconnection.close();
	        } catch (SQLException e) {
	            // handle exception
	        }
	    }
	    return list;
	}

	/*
	 * Get all active users list
	 */

	public List<ProcessnameAtlas> getAllActiveUsers() throws SQLException {
		List<ProcessnameAtlas> list = new ArrayList<>();
		Connection mssqlconnection = databaseService.getConnection();
		Statement stmt = null;
		try {

			stmt = mssqlconnection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"select FIRST_NAME, LAST_NAME, DOMAIN_LOGIN_NAME from users where IS_ACTIVE = 1 order by FIRST_NAME asc");

			int k = 0;

			while (rs.next()) {
				ProcessnameAtlas processnameatlas = new ProcessnameAtlas();

				if (rs.getString("DOMAIN_LOGIN_NAME") == null) {

				} else {

					String firstName = (rs.getString("FIRST_NAME").toString());
					String lastName = (rs.getString("LAST_NAME").toString());
					String domainLoginName = (rs.getString("DOMAIN_LOGIN_NAME").toString());

					String user = firstName + " " + lastName + "-" + domainLoginName;
					processnameatlas.setUserName(user.toString());
				}
				list.add(k, processnameatlas);
				k = k + 1;

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mssqlconnection != null) {
				mssqlconnection.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		System.out.println("in get all active users"+list.toString());
		return list;
	}

	/*
	 * Get column name for selected process
	 */
	public List<String> getColumnNameForSelectedProcess(int processId) throws SQLException {

		ArrayList<String> keys = new ArrayList<>();

		String key;
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();
			pstmt = mssqlconnection.prepareStatement(
					"select process_column_Data from revampAtlas_process_data where PROCESS_ID = ? order by ID desc");
			pstmt.setInt(1, processId);
			boolean runSplit = false;
			rs = pstmt.executeQuery();

			if (rs.next() == false) {
				System.out.println("No result set");
			} else {

				String res = rs.getString(1);

				try {
					JSONObject newUserJSON = new JSONObject(res);

					for (Iterator<String> it = newUserJSON.keys(); it.hasNext();) {
						key = it.next();
						keys.add(key);
					}

				} catch (Exception e) {
					runSplit = true;
				}

				if (runSplit) {
					String[] insertResultData = res.split("\",\"");
					String keyVal;
					try {
						for (int i = 0; i < insertResultData.length; i++) {
							String[] sessionDataAaray = insertResultData[i].split(":");

							if (sessionDataAaray[0].contains("processId") || sessionDataAaray[0].contains("startTime")
									|| sessionDataAaray[0].contains("endTime")
									|| sessionDataAaray[0].contains("duration")
									|| sessionDataAaray[0].contains("isActive")
									|| sessionDataAaray[0].contains("createdBy")
									|| sessionDataAaray[0].contains("createdDate")
									|| sessionDataAaray[0].contains("modifiedBy")
									|| sessionDataAaray[0].contains("modifiedDate")
									|| sessionDataAaray[0].contains("userName")) {
								keyVal = sessionDataAaray[0].replace("\"", "");
								keyVal = keyVal.replace("{", "");
								keyVal = keyVal.replace("}", "");
								keys.add(keyVal);
							} else {
								keyVal = sessionDataAaray[0].replace("\"", "");
								keyVal = keyVal.replace("{", "");
								keyVal = keyVal.replace("}", "");
								keys.add(keyVal);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mssqlconnection != null) {
				mssqlconnection.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if(rs != null) {
				rs.close();
			}
		}

		return keys;
	}
}
