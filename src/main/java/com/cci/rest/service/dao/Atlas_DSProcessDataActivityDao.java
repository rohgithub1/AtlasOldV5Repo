package com.cci.rest.service.dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.ActiveProcessList;
import com.cci.rest.service.dataobjects.AtlasUsers;
import com.cci.rest.service.dataobjects.Atlas_ProcessDataActivity;
import com.opencsv.CSVWriter;

@Component
public class Atlas_DSProcessDataActivityDao {

	@Autowired
	public DatabaseService databaseSerive;

	public Atlas_ProcessDataActivity populateProcessDataActivityObject(int currentProcessId, String username,
			String activityName, String comments) {

		Atlas_ProcessDataActivity processDataActivity = new Atlas_ProcessDataActivity();

		Date Current_Time = null;

		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			mssqlconnection = databaseSerive.getConnection();

			String query = "SELECT TOP 1 END_TIME FROM PROCESS_DATA_ACTIVITY WHERE CREATED_BY = ? AND IS_ACTIVE = 1 ORDER BY 1 DESC";
			pstmt = mssqlconnection.prepareStatement(query);
			pstmt.setString(1, username);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				if (rs.getString("END_TIME") == null) {

				} else {
					try {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
						System.out.println((rs.getTimestamp("END_TIME")));

						Timestamp t = rs.getTimestamp("END_TIME");

						String Current_TimeEST = simpleDateFormat.format(t);
						Current_Time = simpleDateFormat.parse(Current_TimeEST);

						processDataActivity.setStartTime(Current_Time);
						processDataActivity.setActivityName(activityName);
						processDataActivity.setProcessId(currentProcessId);
						processDataActivity.setIsActive(1);
						processDataActivity.setComments(comments);
						processDataActivity.setCreatedBy(username);
						processDataActivity.setCreatedDate(Current_Time);
						processDataActivity.setModifiedDate(Current_Time);
						processDataActivity.setModifiedBy(username);

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}

			return processDataActivity;

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
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public Atlas_ProcessDataActivity populateUpdateProcessDataActivityObject(int currentProcessId, String username,
			String activityName) {
		Atlas_ProcessDataActivity processDataActivity = new Atlas_ProcessDataActivity();

		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

			// get current time in EST format
			Instant timeStamp = Instant.now();
			ZonedDateTime LAZone = timeStamp.atZone(ZoneId.of("America/New_York"));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");

			String Current_TimeEST = LAZone.format(formatter);
//			System.out.println("curredntDateTimeEST : " + Current_TimeEST);
			Date Current_Time = simpleDateFormat.parse(Current_TimeEST);
//			System.out.println("Current_Time : " + Current_Time);
//			System.out.println("End_Time.....  : " + processDataActivity.getEndTime());

			processDataActivity.setActivityName(activityName);
//			System.out.println(processDataActivity.getActivityName());
			processDataActivity.setProcessId(currentProcessId);
			processDataActivity.setCreatedBy(username);
			processDataActivity.setModifiedBy(username);
			processDataActivity.setEndTime(Current_Time);
//			System.out.println(processDataActivity.getEndTime());
			processDataActivity.setModifiedDate(Current_Time);
//			System.out.println(processDataActivity.getModifiedBy());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return processDataActivity;
	}

	public ArrayList<String> selectStartTimeOfActivity(String username) {

		ArrayList<String> result = new ArrayList<String>();

		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			String query = "SELECT START_TIME, END_TIME FROM PROCESS_DATA_ACTIVITY WHERE ID = (  SELECT TOP(1) ID FROM PROCESS_DATA_ACTIVITY  WHERE CREATED_BY= ? AND IS_ACTIVE = 1 ORDER BY 1 DESC)";
			mssqlconnection = databaseSerive.getConnection();
			pstmt = mssqlconnection.prepareStatement(query);
			pstmt.setString(1, username);
			rs = pstmt.executeQuery();

			while (rs.next()) {

				if (rs.getString("START_TIME") == null) {

				} else {
					result.add(rs.getString("START_TIME"));
				}

				result.add(rs.getString("END_TIME"));

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
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean insertProcessDataActivity(Atlas_ProcessDataActivity processDataActivity) {

		String startTime = null;
		String createdDate = null;
		String modifiedDate = null;
		String endTime = null;
		boolean result = true;

		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;

		try {

			mssqlconnection = databaseSerive.getConnection();

			if (processDataActivity.getEndTime() != null) {
				endTime = AtlasUtility.getFormatedDateTimeString(processDataActivity.getEndTime());
			}

			if (processDataActivity.getStartTime() != null) {
				startTime = AtlasUtility.getFormatedDateTimeString(processDataActivity.getStartTime());
			}

			if (processDataActivity.getCreatedDate() != null) {
				createdDate = AtlasUtility.getFormatedDateTimeString(processDataActivity.getCreatedDate());
			}

			if (processDataActivity.getModifiedDate() != null) {
				modifiedDate = AtlasUtility.getFormatedDateTimeString(processDataActivity.getModifiedDate());
			}

			System.out.println("insert into PROCESS_DATA_ACTIVITY");

			String query = "INSERT INTO PROCESS_DATA_ACTIVITY(ACTIVITY_NAME, PROCESS_ID, START_TIME, END_TIME, COMMENTS, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, IS_ACTIVE) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			pstmt = mssqlconnection.prepareStatement(query);

			pstmt.setString(1, processDataActivity.getActivityName().toString().trim());
			pstmt.setInt(2, processDataActivity.getProcessId());
			pstmt.setString(3, startTime);
			pstmt.setString(4, endTime);
			pstmt.setString(5, processDataActivity.getComments().toString().trim());
			pstmt.setString(6, processDataActivity.getCreatedBy().toString().trim());
			pstmt.setString(7, createdDate);
			pstmt.setString(8, processDataActivity.getModifiedBy());
			pstmt.setString(9, modifiedDate);
			pstmt.setInt(10, processDataActivity.getIsActive());

			int count = pstmt.executeUpdate();

			if (count == 1) {
				System.out.println("Activity Inserted Sucessfully");
				result = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public boolean updateProcessDataActivity(Atlas_ProcessDataActivity processDataActivity) {

		String startTime = null;
		String createdDate = null;
		String modifiedDate = null;
		String endTime = null;
		boolean result = false;
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;

		try {

			// Check for null value of date.
			if (processDataActivity.getEndTime() != null) {
				endTime = AtlasUtility.getFormatedDateTimeString(processDataActivity.getEndTime());
			}

			if (processDataActivity.getStartTime() != null) {
				startTime = AtlasUtility.getFormatedDateTimeString(processDataActivity.getStartTime());
			}

			if (processDataActivity.getCreatedDate() != null) {
				createdDate = AtlasUtility.getFormatedDateTimeString(processDataActivity.getCreatedDate());
			}

			if (processDataActivity.getModifiedDate() != null) {
				modifiedDate = AtlasUtility.getFormatedDateTimeString(processDataActivity.getModifiedDate());
			}

			StringBuilder queryString = new StringBuilder("UPDATE PROCESS_DATA_ACTIVITY SET END_TIME = ?");
			if (startTime != null) {
				queryString.append(", START_TIME = ?");
			}
			if (createdDate != null) {
				queryString.append(", Created_Date = ?");
			}
			queryString.append(
					", MODIFIED_DATE = ? WHERE ID = (SELECT TOP(1) ID FROM PROCESS_DATA_ACTIVITY WHERE CREATED_BY = ? AND IS_ACTIVE = 1 ORDER BY 1 DESC) AND END_TIME IS NULL");

			String query = queryString.toString();
			System.out.println("Update process data activity query : " + query);

			mssqlconnection = databaseSerive.getConnection();
			pstmt = mssqlconnection.prepareStatement(query);
			pstmt.setString(1, endTime);
			int parameterIndex = 2;
			if (startTime != null) {
				pstmt.setString(parameterIndex++, startTime);
			}
			if (createdDate != null) {
				pstmt.setString(parameterIndex++, createdDate);
			}
			pstmt.setString(parameterIndex++, modifiedDate);
			pstmt.setString(parameterIndex, processDataActivity.getCreatedBy());
			pstmt.executeUpdate();

			int count = pstmt.getUpdateCount();

			if (count == 1) {
				System.out.println("Activity End Time Updated Successfully");
				result = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = false;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (mssqlconnection != null) {
					mssqlconnection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;

	}

	/*
	 * this method call for each process and activity and gives a report -- created
	 */
	public List<Map<String, Object>> processDataActivityObject(String startDate, String endDate,
			List<ActiveProcessList> alist) {

		// new
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
			mssqlconnection = databaseSerive.getConnection();
			pstmt = mssqlconnection.prepareStatement(
					"select  PROCESS_ID as processId,CREATED_BY, ACTIVITY_NAME as activity_name,FORMAT (CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') as createdDate, DURATION as duration ,IS_ACTIVE "
							+ "from process_data_activity where CREATED_DATE BETWEEN ? and ? AND PROCESS_ID <> 0 AND DURATION IS NOT NULL AND ACTIVITY_NAME not in ('Break') "
							+ "group by  PROCESS_ID,CREATED_BY,ACTIVITY_NAME,DURATION,CREATED_DATE,IS_ACTIVE,CREATED_DATE "
							+ "order by  CREATED_DATE,PROCESS_ID");
			pstmt.setString(1, startdatetime);
			pstmt.setString(2, enddatetime);

			rs = pstmt.executeQuery();
//			System.out.println("result get: "+rs.toString());
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					for (ActiveProcessList p : alist) {
						if (p.getProcessId() == Integer.parseInt(rs.getString("processId")))
							row.put("PROCESS_NAME", p.getProcessName());
					}
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

	
	//this method call for each process_ID and gives all Activity WRT ProcessId -- created
		public List<Map<String, Object>> getProcessDataActivityObject(String ProcessID,List<ActiveProcessList> plist) {	
			List<Map<String,Object>> result=new ArrayList<>();
			Connection mssqlconnection = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				mssqlconnection = databaseSerive.getConnection();
				pstmt = mssqlconnection.prepareStatement("Select ID as id , ACTIVITY_NAME as activity_name,PROCESS_ID as process_id,START_TIME as start_time,END_TIME as end_time,IS_ACTIVE as is_active, "+
														"CREATED_BY as created_by,CREATED_DATE as created_date , DURATION as duration from process_data_activity where process_id in (?) and CREATED_DATE>='2024-07-20 23:59:59'");
				pstmt.setString(1,ProcessID);
				
				rs=pstmt.executeQuery();
				
				while(rs.next())
				{
					Map<String,Object> row = new HashMap<>();
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						for (ActiveProcessList p : plist) {
							if (p.getProcessId() == Integer.parseInt(rs.getString("process_id")))
								row.put("PROCESS_NAME", p.getProcessName());
						}
						row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
					}
					result.add(row);
				}
				
				System.out.println("list = "+result.toString());
				rs.close();
			}catch(Exception e)
			{
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
}
