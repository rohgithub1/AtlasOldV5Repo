package com.cci.rest.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import com.cci.rest.service.database.config.DatabaseService;


@Component
public class Dashboard_ActivityDAO {

	@Autowired
	private DatabaseService databaseService;

	public List<Map<String, Object>> getOverAllQueueViewdashboardData(String startDate, String endDate) throws SQLException {

		List<Map<String,Object>> result = new ArrayList<>();
	//	String startdate = startDate.substring(0, startDate.indexOf('T'));
	//  String enddate = endDate.substring(0, endDate.indexOf('T'));
		String startdatetime = startDate+ " 00:00:00";
		String enddatetime = endDate + " 23:59:59";
		
		System.out.println(startdatetime);
		System.out.println(enddatetime);
		
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
				

			mssqlconnection = databaseService.getConnection();
			
			//pstmt = mssqlconnection.prepareStatement("SELECT p.Process AS PROCESS_NAME, SUM(CASE WHEN t.ACTIVITY_NAME = 'Login' THEN 1 ELSE 0 END) AS LOGIN_COUNT, COUNT(rd.PROCESS_ID) AS ON_JOB, SUM(CASE WHEN t.ACTIVITY_NAME = 'HUD' THEN 1 ELSE 0 END) AS HUD_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = '121' THEN 1 ELSE 0 END) AS ONE_TWO_ONE_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'Meet' THEN 1 ELSE 0 END) AS COA_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'COA' THEN 1 ELSE 0 END) AS COA_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'BRK1' THEN 1 ELSE 0 END) AS BRK1_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'BRK2' THEN 1 ELSE 0 END) AS BRK2_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'UBK' THEN 1 ELSE 0 END) AS UBK_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'Tech Issue' THEN 1 ELSE 0 END) AS TECH_ISSUE_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'TGIF' THEN 1 ELSE 0 END) AS TGIF_COUNT, COUNT(DISTINCT t.CREATED_BY) AS TOTAL_AGENT FROM process_data_activity t JOIN PROCESSES p ON t.PROCESS_ID = p.ID LEFT JOIN revampAtlas_process_data rd ON t.PROCESS_ID = rd.PROCESS_ID WHERE t.START_TIME >= '"+StartDate+"' AND t.END_TIME <= '"+EndDate+"' AND p.IS_ACTIVE = 1 AND t.PROCESS_ID <> 1  GROUP BY p.Process ORDER BY p.Process;");
//			pstmt = mssqlconnection.prepareStatement("SELECT pr.process as QUEUE_NAME, (SUM(CASE WHEN t.ACTIVITY_NAME is not null THEN 1 ELSE 0 END)) AS AGENT_ON_QUEUE_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'PRODUCTION ACTIVITY' THEN 1 ELSE 0 END) AS ON_JOB_COUNT ,SUM(CASE WHEN t.ACTIVITY_NAME = 'HUD' THEN 1 ELSE 0 END) AS HUD_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'MEET' THEN 1 ELSE 0 END) AS MEET_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME LIKE '%BRK%' THEN 1 ELSE 0 END) AS BREAK_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = '121' THEN 1 ELSE 0 END) AS ONE_ON_ONE_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'COA' THEN 1 ELSE 0 END) AS COA_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'TGIF' THEN 1 ELSE 0 END) AS TGIF_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'Tech Issue' THEN 1 ELSE 0 END) AS TECH_ISSUE_COUNT, SUM(CASE WHEN t.ACTIVITY_NAME = 'Job Monitoring' THEN 1 ELSE 0 END) AS JOB_MONITORING_COUNT FROM process_data_activity t JOIN Processes pr ON t.process_id = pr.id WHERE t.IS_ACTIVE = 1 AND pr.process <> 'Admin' AND pr.IS_ACTIVE = 1 AND t.START_TIME >= ? AND t.END_TIME is null GROUP BY t.process_id, pr.process order by pr.process");
			pstmt = mssqlconnection.prepareStatement(" "
					+ "With Processdata As (  "
					+ "SELECT COALESCE(RP.PROCESS_ID, PA.PROCESS_ID) AS PROCESS_ID, "
					+ "       COALESCE(RP.Total_Process_Job_Count, PA.Total_Process_Job_Count) AS Total_Process_Job_Count,  "
					+ "       COALESCE(RP.Average_Handling_Time, PA.Average_Handling_Time) AS Average_Handling_Time  "
					+ "FROM (  "
					+ "SELECT Process_Id, "
					+ "    COUNT( Process_Id) AS Total_Process_Job_Count, "
					+ "    CONVERT(VARCHAR(8), DATEADD(SECOND, SUM(DATEDIFF(SECOND, '00:00:00', TRY_CAST(Duration AS TIME))) / NULLIF(COUNT(*), 0), '00:00:00'), 108) AS Average_Handling_Time "
					+ "    FROM revampAtlas_Process_data  where IS_ACTIVE = '1' AND START_TIME >= ?  "
					+ "    group BY Process_Id "
					+ ") AS RP "
					+ "  "
					+ "FULL OUTER JOIN "
					+ "  "
					+ "( SELECT PROCESS_ID, "
					+ "        '0' AS Total_Process_Job_Count, "
					+ "        '00:00:00' AS Average_Handling_Time  "
					+ "FROM process_data_activity  "
					+ "WHERE START_TIME >= ? AND PROCESS_ID <> 0  "
					+ "GROUP BY PROCESS_ID "
					+ ") AS PA  "
					+ "ON RP.PROCESS_ID = PA.PROCESS_ID AND RP.PROCESS_ID <> 0 AND PA.PROCESS_ID <> 0 "
					+ ") "
					+ "    SELECT pr.process as QUEUE_NAME,  "
					+ "    SUM(CASE WHEN t.Activity_Name IS NOT NULL THEN 1 ELSE 0 END) AS AGENT_ON_QUEUE_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = 'PRODUCTION ACTIVITY' THEN 1 ELSE 0 END) AS ON_JOB_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = 'HUD' THEN 1 ELSE 0 END) AS HUD_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = 'MEET' THEN 1 ELSE 0 END) AS MEET_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name LIKE '%BRK%' THEN 1 ELSE 0 END) AS BREAK_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = '121' THEN 1 ELSE 0 END) AS ONE_ON_ONE_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = 'COA' THEN 1 ELSE 0 END) AS COA_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = 'TGIF' THEN 1 ELSE 0 END) AS TGIF_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = 'Tech Issue' THEN 1 ELSE 0 END) AS TECH_ISSUE_COUNT,  "
					+ "    SUM(CASE WHEN t.Activity_Name = 'Job Monitoring' THEN 1 ELSE 0 END) AS JOB_MONITORING_COUNT, "
					+ "    SUM(CASE WHEN t.Activity_Name = 'QA' THEN 1 ELSE 0 END) AS QA_COUNT, "
					+ "    Rt.Total_Process_Job_Count AS Volume, "
					+ "    Rt.Average_Handling_Time as Average_Handling_Time "
					+ "        FROM process_data_activity t  "
					+ "    FULL OUTER JOIN Processdata Rt ON Rt.process_Id = t.Process_Id   "
					+ "    JOIN  Processes pr ON pr.Id = COALESCE(t.Process_Id, Rt.Process_Id) "
					+ "    WHERE   "
					+ "        t.START_TIME >= ? AND t.END_TIME is null  "
					+ "        AND t.IS_ACTIVE = 1  "
					+ "    AND pr.process <> 'Admin'  "
					+ "    AND pr.IS_ACTIVE = 1  "
					+ "    GROUP BY "
					+ "        t.PROCESS_ID,Rt.Total_Process_Job_Count,Rt.Average_Handling_Time,pr.process  "
					+ "        ORDER BY  "
					+ "    pr.process;");
			pstmt.setString(1, startdatetime);
			pstmt.setString(2, startdatetime);
			pstmt.setString(3, startdatetime);
			rs = pstmt.executeQuery();
			System.out.println(pstmt.toString());
			while(rs.next()) {
				Map<String, Object> row = new HashMap<>();
				for(int i=1; i<=rs.getMetaData().getColumnCount();i++) {
					row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
				}
				result.add(row);
			}
			
		}catch(Exception e) {
			throw e;
		}finally {
			if(rs!=null) {
				rs.close();
			}if(pstmt!=null) {
				pstmt.close();
			}
			if(mssqlconnection !=null) {
				mssqlconnection.close();
			}
		}
		
		return result;
	}



	/*
	 * this method call for each process_ID and gives all Activity WRT ProcessId -- created by sid
	 */
	
	public List<Map<String, Object>> getProcessDataActivityObject(String startTime, String endTime) {
	    List<Map<String, Object>> result = new ArrayList<>();
	    Connection mssqlconnection = null;
	    Statement stmt = null;
	    ResultSet rs = null;
	    ResultSet rs2 = null;
	    ResultSet rs3 = null;
	    ResultSet rs4 = null;
	    
	    // Define the UTC and EST time zones
	    ZoneId istZoneId = ZoneId.of("Asia/Kolkata");
        ZoneId estZoneId = ZoneId.of("America/New_York");
	 
	    System.out.println(startTime + " " + endTime);

	    // SQL queries
//	    String sql = "WITH ActivitySummary AS (SELECT PROCESS_ID, CREATED_BY, MIN(CASE WHEN ACTIVITY_NAME = 'Login' THEN START_TIME END) AS Login_Time, MAX(CASE WHEN ACTIVITY_NAME = 'Logout' THEN START_TIME ELSE NULL END) AS Logout_Time, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'HUD' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'HUD' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'HUD' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS HUD, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Meet' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Meet' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Meet' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS Meet, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'COA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'COA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'COA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS COA, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Tech Issue' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Tech Issue' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Tech Issue' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS Tech_Issue, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME IN ('BRK1', 'BRK2', 'UBK') THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME IN ('BRK1', 'BRK2', 'UBK') THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME IN ('BRK1', 'BRK2', 'UBK') THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS Breaks, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = '121' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = '121' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = '121' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS OneToOne, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'TGIF' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'TGIF' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'TGIF' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS TGIF FROM process_data_activity WHERE ACTIVITY_NAME IN ('HUD', 'Meet', 'COA', 'Tech Issue', 'BRK1', 'BRK2', 'UBK', 'Login', 'Logout', '121', 'TGIF') AND END_TIME IS NOT NULL AND START_TIME >= '" + startTime + "' AND END_TIME < '" + endTime + "' GROUP BY PROCESS_ID, CREATED_BY) SELECT PDA.PROCESS_ID, PDA.CREATED_BY, ISNULL(CONVERT(VARCHAR(8), ASUM.Login_Time, 108), '00:00:00') AS Login_Time, PDA.Total_Processes AS \"Jobs Processed\", PDA.Total_Duration_By_Created_By AS \"Total Production Time\", PDA.Average_Handling_Time AS \"Average Handling Time\", ISNULL(ASUM.HUD, '00:00:00') AS HUD, ISNULL(ASUM.Meet, '00:00:00') AS Meet, ISNULL(ASUM.COA, '00:00:00') AS COA, ISNULL(ASUM.Tech_Issue, '00:00:00') AS Tech_Issue, ISNULL(ASUM.Breaks, '00:00:00') AS Breaks, ISNULL(ASUM.OneToOne, '00:00:00') AS OneToOne, ISNULL(ASUM.TGIF, '00:00:00') AS TGIF, ISNULL(CONVERT(VARCHAR(8), ASUM.Logout_Time, 108), '00:00:00') AS Logout_Time FROM (SELECT PROCESS_ID, CREATED_BY, COUNT(*) AS Total_Processes, CONVERT(VARCHAR(8), DATEADD(SECOND, SUM(DATEDIFF(SECOND, '00:00:00', TRY_CAST(duration AS TIME))), '00:00:00'), 108) AS Total_Duration_By_Created_By, CONVERT(VARCHAR(8), DATEADD(SECOND, SUM(DATEDIFF(SECOND, '00:00:00', TRY_CAST(duration AS TIME))) / NULLIF(COUNT(*), 0), '00:00:00'), 108) AS Average_Handling_Time FROM revampAtlas_process_data WHERE START_TIME >= '" + startTime + "' AND END_TIME < '" + endTime + "' AND duration IS NOT NULL GROUP BY PROCESS_ID, CREATED_BY) AS PDA LEFT JOIN ActivitySummary AS ASUM ON PDA.PROCESS_ID = ASUM.PROCESS_ID AND PDA.CREATED_BY = ASUM.CREATED_BY ORDER BY PDA.PROCESS_ID, PDA.CREATED_BY;";
	    String sql="WITH ActivitySummary AS (SELECT PROCESS_ID, CREATED_BY, MIN(CASE WHEN ACTIVITY_NAME = 'Login' THEN START_TIME END) AS Login_Time, MAX(CASE WHEN ACTIVITY_NAME = 'Logout' THEN START_TIME ELSE NULL END) AS Logout_Time, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'HUD' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'HUD' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'HUD' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS HUD, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Meet' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Meet' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Meet' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS Meet, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'COA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'COA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'COA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS COA, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Tech Issue' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Tech Issue' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Tech Issue' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS Tech_Issue, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME IN ('BRK1', 'BRK2', 'UBRK') THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME IN ('BRK1', 'BRK2', 'UBRK') THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME IN ('BRK1', 'BRK2', 'UBRK') THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS Breaks, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = '121' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = '121' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = '121' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS OneToOne, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'TGIF' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'TGIF' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'TGIF' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS TGIF, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Job Monitoring' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Job Monitoring' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'Job Monitoring' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS Job_Monitoring, CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'QA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 3600) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'QA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) / 60 % 60), 2) + ':' + RIGHT('00' + CONVERT(VARCHAR, SUM(CASE WHEN ACTIVITY_NAME = 'QA' THEN DATEDIFF(SECOND, START_TIME, END_TIME) ELSE 0 END) % 60), 2) AS QA FROM process_data_activity WHERE ACTIVITY_NAME IN ('HUD', 'Meet', 'COA', 'Tech Issue', 'BRK1', 'BRK2', 'UBRK', 'Login', 'Logout', '121', 'TGIF','Job Monitoring','QA')AND END_TIME IS NOT NULL AND START_TIME BETWEEN '"+startTime+"' AND '"+endTime+"' GROUP BY PROCESS_ID, CREATED_BY), ProcessSummary AS (SELECT COALESCE(RP.PROCESS_ID, PA.PROCESS_ID) AS PROCESS_ID, COALESCE(RP.CREATED_BY, PA.CREATED_BY) AS CREATED_BY, COALESCE(RP.Total_Processes, PA.Total_Processes) AS Total_Processes, COALESCE(RP.Total_Duration_By_Created_By, PA.Total_Duration_By_Created_By) AS Total_Duration_By_Created_By, COALESCE(RP.Average_Handling_Time, PA.Average_Handling_Time) AS Average_Handling_Time FROM (SELECT PROCESS_ID, CREATED_BY, COUNT(*) AS Total_Processes, CONVERT(VARCHAR(8), DATEADD(SECOND, SUM(DATEDIFF(SECOND, '00:00:00', TRY_CAST(duration AS TIME))), '00:00:00'), 108) AS Total_Duration_By_Created_By, CONVERT(VARCHAR(8), DATEADD(SECOND, SUM(DATEDIFF(SECOND, '00:00:00', TRY_CAST(duration AS TIME))) / NULLIF(COUNT(*), 0), '00:00:00'), 108) AS Average_Handling_Time FROM revampAtlas_process_data WHERE START_TIME BETWEEN '"+startTime+"' AND '"+endTime+"' AND PROCESS_ID <> 0 GROUP BY PROCESS_ID, CREATED_BY) AS RP Right JOIN (SELECT PROCESS_ID, CREATED_BY, '0' AS Total_Processes, '00:00:00' AS Total_Duration_By_Created_By, '00:00:00' AS Average_Handling_Time FROM process_data_activity WHERE START_TIME BETWEEN '"+startTime+"' AND '"+endTime+"' AND PROCESS_ID <> 0 AND End_Time is null GROUP BY PROCESS_ID, CREATED_BY) AS PA ON RP.PROCESS_ID = PA.PROCESS_ID AND RP.CREATED_BY = PA.CREATED_BY AND RP.PROCESS_ID <> 0 AND PA.PROCESS_ID <> 0) SELECT PDA.PROCESS_ID, PDA.CREATED_BY, ISNULL(CONVERT(VARCHAR(8), ASUM.Login_Time, 108), '00:00:00') AS Login_Time, PDA.Total_Processes AS \"Jobs Processed\", PDA.Total_Duration_By_Created_By AS \"Total Production Time\", PDA.Average_Handling_Time AS \"Average Handling Time\", ISNULL(ASUM.HUD, '00:00:00') AS HUD, ISNULL(ASUM.Meet, '00:00:00') AS Meet, ISNULL(ASUM.COA, '00:00:00') AS COA, ISNULL(ASUM.Tech_Issue, '00:00:00') AS Tech_Issue, ISNULL(ASUM.Breaks, '00:00:00') AS Breaks, ISNULL(ASUM.OneToOne, '00:00:00') AS OneToOne, ISNULL(ASUM.TGIF, '00:00:00') AS TGIF, ISNULL(ASUM.Job_Monitoring, '00:00:00') AS Job_Monitoring, ISNULL(ASUM.QA, '00:00:00') AS QA FROM ProcessSummary PDA left JOIN ActivitySummary ASUM ON PDA.PROCESS_ID = ASUM.PROCESS_ID AND PDA.CREATED_BY = ASUM.CREATED_BY ORDER BY PDA.PROCESS_ID, PDA.CREATED_BY;";

	    System.out.println("the query executing is="+sql);
	    String sqlDeliveredHours = "SELECT CREATED_BY, RIGHT('0' + CAST(DATEDIFF(SECOND, MIN(START_TIME), MAX(END_TIME)) / 3600 AS VARCHAR), 2) + ':' + RIGHT('0' + CAST((DATEDIFF(SECOND, MIN(START_TIME), MAX(END_TIME)) % 3600) / 60 AS VARCHAR), 2) + ':' + RIGHT('0' + CAST(DATEDIFF(SECOND, MIN(START_TIME), MAX(END_TIME)) % 60 AS VARCHAR), 2) AS Delivered_Hours FROM process_data_activity WHERE START_TIME >= '" + startTime + "' AND END_TIME IS NOT NULL GROUP BY CREATED_BY;";

	    String sqlCurrentStatus = "WITH RankedActivities AS ( SELECT  CREATED_BY, Activity_Name, CONVERT(VARCHAR(8), DATEADD(SECOND, DATEDIFF(SECOND, Start_Time, GETDATE()), 0), 108) AS Status_Time, ROW_NUMBER() OVER (PARTITION BY CREATED_BY ORDER BY Start_Time DESC) AS rn FROM process_data_activity WHERE START_TIME >='" + startTime + "' AND END_TIME IS NULL) SELECT CREATED_BY, Activity_Name, Status_Time FROM RankedActivities WHERE rn = 1";
	    
	    String sqlLogTime = "WITH ActivitySummary AS (SELECT CREATED_BY, MIN(CASE WHEN ACTIVITY_NAME = 'Login' THEN START_TIME END) AS Login_Time, MAX(CASE WHEN ACTIVITY_NAME = 'Logout' THEN START_TIME ELSE NULL END) AS Logout_Time FROM process_data_activity WHERE ACTIVITY_NAME IN ('Login', 'Logout') AND END_TIME IS NOT NULL AND START_TIME >= '" + startTime + "' AND END_TIME < '" + endTime + "' GROUP BY CREATED_BY) SELECT PDA.PROCESS_ID, PDA.CREATED_BY, ISNULL(CONVERT(VARCHAR(8), ASUM.Login_Time, 108), '00:00:00') AS Login_Time, ISNULL(CONVERT(VARCHAR(8), ASUM.Logout_Time, 108), '00:00:00') AS Logout_Time FROM (SELECT PROCESS_ID, CREATED_BY, COUNT(*) AS Total_Processes, CONVERT(VARCHAR(8), DATEADD(SECOND, SUM(DATEDIFF(SECOND, '00:00:00', TRY_CAST(duration AS TIME))), '00:00:00'), 108) AS Total_Duration_By_Created_By, CONVERT(VARCHAR(8), DATEADD(SECOND, SUM(DATEDIFF(SECOND, '00:00:00', TRY_CAST(duration AS TIME))) / NULLIF(COUNT(*), 0), '00:00:00'), 108) AS Average_Handling_Time FROM revampAtlas_process_data WHERE START_TIME >= '" + startTime + "' AND END_TIME < '" + endTime + "' AND duration IS NOT NULL GROUP BY PROCESS_ID, CREATED_BY) AS PDA INNER JOIN ActivitySummary AS ASUM ON PDA.CREATED_BY = ASUM.CREATED_BY ORDER BY PDA.PROCESS_ID, PDA.CREATED_BY;";
 	    
	    try {
	        mssqlconnection = databaseService.getConnection();
	        stmt = mssqlconnection.createStatement();

	        // Fetch results from the existing query
	        rs = stmt.executeQuery(sql);
	        while (rs.next()) {
	            Map<String, Object> row = new HashMap<>();
	            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
	                row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
	            }
	            String createdBy = (String) row.get("CREATED_BY");
	       
	            result.add(row);
	         }

	        // Fetch results from the Delivered_Hours query
	        rs2 = stmt.executeQuery(sqlDeliveredHours);
	        Map<String, String> deliveredHoursMap = new HashMap<>();
	        while (rs2.next()) {
	            String createdBy = rs2.getString("CREATED_BY");
	            String deliveredHours = rs2.getString("Delivered_Hours");
	            deliveredHoursMap.put(createdBy, deliveredHours);
	        }

	        // Fetch results from the Current Status query
	        rs3 = stmt.executeQuery(sqlCurrentStatus);
	        Map<String, String> currentStatusMap = new HashMap<>();
	        while (rs3.next()) {
	        	String activity_Name = rs3.getString("Activity_Name");
	        	String status_Time = rs3.getString("Status_Time"); 	
	        	String createdBy = rs3.getString("CREATED_BY");
	            
	        	  // Parse the time assuming it is in IST
                LocalTime istLocalTime = LocalTime.parse(status_Time, DateTimeFormatter.ofPattern("HH:mm:ss"));

                // Create a ZonedDateTime with the current date and IST time zone
                ZonedDateTime istZonedDateTime = ZonedDateTime.of(istLocalTime.atDate(java.time.LocalDate.now()), istZoneId);

                // Convert IST time to EST time
                ZonedDateTime estZonedDateTime = istZonedDateTime.withZoneSameInstant(estZoneId);

                // Format the EST time
                String estTimeString = estZonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                // Create the current status string
                String current_Status = activity_Name + " - " + estTimeString;
	        	
	            currentStatusMap.put(createdBy, current_Status);
	        }
	        
	        // Fetch results from the LoginTime Query
	        rs4 = stmt.executeQuery(sqlLogTime);
	        Map<String, String> loginTimeMap = new HashMap<>();
	        Map<String, String> logoutTimeMap = new HashMap<>();
	        while (rs4.next()) {
	            String createdBy = rs4.getString("CREATED_BY");
	            String login_time = rs4.getString("Login_Time");
	            String logout_time = rs4.getString("Logout_Time");
	            loginTimeMap.put(createdBy, login_time);
	            logoutTimeMap.put(createdBy, logout_time);
	        }
	        
	        
	        // Add deliveredHours, currentStatus, LoginTime and LogoutTime to each map in the result list
	        for (Map<String, Object> row : result) {
	            String createdBy = (String) row.get("CREATED_BY");

	            row.put("Delivered_Hours", deliveredHoursMap.getOrDefault(createdBy, "00:00:00"));

	            row.put("Current_Status", currentStatusMap.getOrDefault(createdBy, "No Current Status"));
	            
	            row.put("Login_Time", loginTimeMap.getOrDefault(createdBy, "00:00:00"));
	            
	            row.put("Logout_Time", logoutTimeMap.getOrDefault(createdBy, "00:00:00"));
	        }
	        
	      	        
	       
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (rs2 != null) rs2.close();
	            if (rs3 != null) rs3.close();
	            if (rs4 != null) rs4.close();
	            if (stmt != null) stmt.close();
	            if (mssqlconnection != null) mssqlconnection.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    return result;
	}

}