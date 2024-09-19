package com.cci.rest.service.Scheduler;

import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.cci.rest.service.Authenticate.LoginAuthenticator;
import com.cci.rest.service.dao.AtlasGetProcessDataDAO;
import com.cci.rest.service.dao.AtlasUtility;
import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.ActiveProcessList;
import com.cci.rest.service.dataobjects.AtlasUsers;
import com.cci.rest.service.dataobjects.ProcessnameAtlas;
import com.cci.rest.service.mail.AtlasMailRequest;
import com.opencsv.CSVWriter;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cci.rest.service.mail.AtlasMailService;

@Component
public class Scheduler {

	@Autowired
	private DatabaseService databaseService;

	@Autowired
	AtlasMailService atlasmailservice;

	@Autowired
	AtlasGetProcessDataDAO selectatlasprocessdao;

	@Autowired
	LoginAuthenticator loginautheticator;

	private String fromMail = "";
	private String toMail = "";
	private String ccmail = "";
	private String Body;
	String[] ccMail, ToMail = null;
	private String subject = "";

//	@Scheduled(cron = "0 0/30 * * * ?")
	@Scheduled(cron = "0 30 23 ? * * ")
	public void scheduleTaskWithFixedRate() {

		System.out.println("Schedular Working...!");

		getDailyActivityReport();
	}

	/*
	 * * to find all users daily login and logout dateTime
	 */
	public <DAILY_LOGINLOGOUT_REPORT_EMAIL_FROM> List<Map<String, Object>> GetDailyReportLoginLogout(String startDate,
			String endDate, Map<String, String> props) {

		List<Map<String, Object>> list = new ArrayList<>();
		String sdate2 = startDate;
		String edate2 = endDate;
//		test
//		sdate2 = "07/07/2024 00:00:00";
//		edate2 = "07/07/2024 23:59:59";
		Connection mssqlconnection = null;

		String selectActivityReportQuery = "select ID as id, ACTIVITY_NAME as activity_name, PROCESS_ID as processId, FORMAT (START_TIME, 'yyyy-MM-dd HH:mm:ss') as startTime, FORMAT (END_TIME, 'yyyy-MM-dd HH:mm:ss')  as endTime, COMMENTS as comments, IS_ACTIVE as isActive, CREATED_BY as createdBy, FORMAT (CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') as createdDate, MODIFIED_BY as modifiedBy, FORMAT (MODIFIED_DATE, 'yyyy-MM-dd HH:mm:ss') as modifiedDate, DURATION as duration";
		selectActivityReportQuery += " from process_data_activity where created_date between '" + sdate2 + "' and '"
				+ edate2 + "'";

		Map<String, Object> loginmap = new TreeMap<>();
		Map<String, Object> logoutmap = new HashMap<>();
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			mssqlconnection = databaseService.getConnection();
			PreparedStatement pstmt = mssqlconnection.prepareStatement(selectActivityReportQuery);
			ResultSet rs1 = pstmt.executeQuery();
			while (rs1.next()) {
				String createdBy = rs1.getString("createdBy");
				System.out.println("createdBy = " + createdBy + " , " + rs1.getString("Id") + " , "
						+ rs1.getString("activity_name"));
				if (rs1.getString("activity_name").equalsIgnoreCase("Login")) {
					if (loginmap.containsKey(createdBy + "Login")) {
						try {
							String jsonString = loginmap.get(createdBy + "Login").toString();
							JSONObject jsonObject = new JSONObject(jsonString);
							String loginTime = jsonObject.getString("LoginTime");
							LocalDateTime dateTime = LocalDateTime.parse(loginTime, formatter);
							LocalDateTime dateTime2 = LocalDateTime.parse(rs1.getString("startTime"), formatter);
							if (dateTime.isAfter(dateTime2)) {
								String formattedDateTime = dateTime2.format(formatter);
								jsonObject.put("LoginTime", formattedDateTime);
								loginmap.put(createdBy + "Login", jsonObject.toString());

							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (!loginmap.containsKey(createdBy + "Login")) {
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
						loginmap.put(createdBy + "Login", add.toString());
					}
				}
				if (logoutmap.containsKey(createdBy + "Logout")) {
					System.out.println("created BY = " + createdBy + " , " + rs1.getString("id"));
					try {
						String jsonString = logoutmap.get(createdBy + "Logout").toString();
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
							System.out.println("dateTime2 " + dateTime2);
							if (dateTime.isAfter(dateTime2)) {
								String formattedDateTime = dateTime.format(formatter);
								jsonObject.put("LogoutTime", formattedDateTime);
								logoutmap.put(createdBy + "Logout", jsonObject.toString());
							}
						} else {
							String formattedDateTime = dateTime.format(formatter);
							jsonObject.put("LogoutTime", formattedDateTime);
							logoutmap.put(createdBy + "Logout", jsonObject.toString());
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (!logoutmap.containsKey(createdBy + "Logout")) {
					StringBuilder logout = new StringBuilder();
					logout.append("{");
					logout.append("\"Id\": \"").append(rs1.getString("id")).append("\",");
					if (rs1.getString("activity_name").equalsIgnoreCase("Logout"))
						logout.append("\"activity_name\": \"").append(rs1.getString("activity_name")).append("\",");
					else
						logout.append("\"activity_name\": \"").append("Logout(" + rs1.getString("activity_name") + ")")
								.append("\",");
					if (!rs1.getString("endTime").equals(null))
						logout.append("\"LogoutTime\": \"").append(rs1.getString("endTime")).append("\",");
					else
						logout.append("\"LogoutTime\": \"").append(rs1.getString("startTime")).append("\",");
					logout.append("\"createdBy\": \"").append(createdBy).append("\",");
					logout.append("\"isActive\": \"").append(rs1.getString("isActive")).append("\",");
					if (logout.charAt(logout.length() - 1) == ',') {
						logout.deleteCharAt(logout.length() - 1);
					}
					logout.append("}");
					logoutmap.put(createdBy + "logout", logout.toString());

				}
			}
			loginmap.putAll(logoutmap);
			// System.out.println("map map = "+loginmap.toString());
			for (Map.Entry<String, Object> entry : loginmap.entrySet()) {
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put(entry.getKey(), entry.getValue());
				list.add(tempMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

//			 For File Name
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date()); // Using today's date
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String currentDate = dateFormat.format(calendar.getTime());
		currentDate = currentDate.replace('/', '-');
		String fileName = "C:\\Atlas_Daily_Report" + "\\DailyLoginLogoutReport_" + currentDate + ".csv";

		System.out.println("fileName + " + fileName);
		File file = new File(fileName);

		boolean dataPresence = true;

		dataPresence = list.isEmpty();
		try {
			FileWriter outputfile = new FileWriter(file);
			CSVWriter writer = new CSVWriter(outputfile);

			String[] text = { "No Data Present for " + currentDate };
			if (dataPresence) {
				writer.writeNext(text, true);
				writer.close();
			} else {
				String[] header = { "ID", "CreatedBy", "Activity_Name", "DateTime"};
				writer.writeNext(header);

				for (Map<String, Object> record : list) {
					for (Map.Entry<String, Object> entry : record.entrySet()) {
						String key = entry.getKey();
						Object val = entry.getValue();
						String jsonString = val.toString();
						JSONObject jsonObject = new JSONObject(jsonString);
						String Id = "";
						String createdBy = "";
						String activityName = jsonObject.get("activity_name").toString();
						String dateTime = "";
						
						if (activityName.equals("Login")) {
							Id = jsonObject.get("Id").toString();
							createdBy = jsonObject.get("createdBy").toString();
							dateTime = jsonObject.get("LoginTime").toString();

						} else {
							Id = jsonObject.get("Id").toString();
							dateTime = jsonObject.get("LogoutTime").toString();
							createdBy = jsonObject.get("createdBy").toString();
						}
						String[] row = { Id, createdBy, activityName, dateTime};
						writer.writeNext(row);

					}
				}
				writer.close();
			}
			AtlasMailRequest atlasMailRequest = new AtlasMailRequest();
			subject = props.get("DAILY_ACTIVITY_REPORT_EMAIL_SUBJECT");
			Body = props.get("DAILY_ACTIVITY_REPORT_EMAIL_BODY");
//			    			fromMail = props.get("DAILY_ACTIVITY_REPORT_EMAIL_FROM");
			fromMail = "amoshind@crosscountry.com";
//			    			toMail = props.get("DAILY_ACTIVITY_REPORT_EMAIL_TO");
			toMail = "General - Stuti Test <18c163c3.crosscountry.com@amer.teams.ms>";
			ToMail = toMail.split(";");
			ccmail = "General - Stuti Test <18c163c3.crosscountry.com@amer.teams.ms>";
			ccMail = ccmail.split(";");
			atlasMailRequest.setTo(ToMail);
			atlasMailRequest.setCc(ccMail);
			atlasMailRequest.setFrom(fromMail);
			atlasMailRequest.setSubject(subject);
			atlasMailRequest.setBody(Body);
			atlasMailRequest.setAttachment("DailyLoginLogoutReport_" + currentDate + ".csv");
			atlasmailservice.sendEmail(atlasMailRequest);
			System.out.println("mail send");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public <DAILY_ACTIVITY_REPORT_EMAIL_FROM> List<Map<String, Object>> getDailyActivityReport() {
		Map<String, String> propMap = new HashMap<String, String>();

		List<Map<String, Object>> result = new ArrayList<>();

		try {

			Connection mssql = databaseService.getConnection();
			Statement stmt = mssql.createStatement();

			String query = "select MAP_FROM , MAP_TO from  mapping_data where is_active='Y'";
			ResultSet rs1 = stmt.executeQuery(query);

			ArrayList<Object> data = null;

			ResultSetMetaData rsmd = rs1.getMetaData();
			int numOfCols = rsmd.getColumnCount();
			data = new ArrayList<Object>();

			while (rs1.next()) {

				ArrayList<Object> temp = new ArrayList<Object>();// Creating arraylist

				for (int col = 1; col <= numOfCols; col++) {
					String str = rs1.getString(col);
					if (str == null) { // if the column has null value
						temp.add("");
					} else {
						temp.add(str.trim());
					}
				}
//				System.out.println(temp);
				data.add(temp);

			}

			for (int i = 0; i < data.size(); i++) {
				ArrayList<Object> AL = (ArrayList) data.get(i);
				propMap.put(String.valueOf(AL.get(0)), String.valueOf(AL.get(1)));
			}

			rs1.close();

//		    Start Time

			String startDay = propMap.get("DAILY_ACTIVITY_REPORT_NUMBER_OF_DAYS_START");
			System.out.println("start day " + startDay);

			int noOfDays_Start = Integer.parseInt(startDay);

			Date tempStartDate = null, tempEndDate = null;
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date()); // Using today's date

			cal.add(Calendar.DATE, -(noOfDays_Start));
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			String sdate1 = sdf.format(cal.getTime());

			String sdate2 = sdate1 + " 00:00:00";

			tempStartDate = AtlasUtility.getFormatedSqlDateTimeScheduler(sdate2);

//			End Time

			int noOfDays_End = 0;
			cal.setTime(new Date());
			cal.add(Calendar.DATE, -(noOfDays_Start));
			SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");
			String edate1 = sdf2.format(cal.getTime());

			String edate2 = edate1 + " 23:59:59";

			tempEndDate = AtlasUtility.getFormatedSqlDateTimeScheduler(edate2);

//			sdate2="06/14/2023 00:00:00";
//			edate2="06/14/2023 23:59:59";
			String selectActivityReportQuery = "select ID as id, ACTIVITY_NAME as activity_name, PROCESS_ID as processId,"
					+ " FORMAT (START_TIME, 'yyyy-MM-dd HH:mm:ss') as startTime, FORMAT (END_TIME, 'yyyy-MM-dd HH:mm:ss')  as endTime, "
					+ "COMMENTS as comments, IS_ACTIVE as isActive, CREATED_BY as createdBy, FORMAT (CREATED_DATE, 'yyyy-MM-dd HH:mm:ss') as createdDate,"
					+ " MODIFIED_BY as modifiedBy, FORMAT (MODIFIED_DATE, 'yyyy-MM-dd HH:mm:ss') as modifiedDate, DURATION as duration";
			selectActivityReportQuery += " from process_data_activity  where created_date between '" + sdate2 + "'and'"
					+ edate2 + "'";

			ResultSet rs = stmt.executeQuery(selectActivityReportQuery);

			// For File Name
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date()); // Using today's date

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String currentDate = dateFormat.format(calendar.getTime());

			currentDate = currentDate.replace('/', '-');

			String fileName = propMap.get("DAILY_ACTIVITY_REPORT_STAGE_FILE_PATH") + currentDate.substring(0, 10)
					+ ".csv";

			File file = new File(fileName);
			FileWriter outputfile = new FileWriter(file);

			CSVWriter writer = new CSVWriter(outputfile);

			boolean dataPresence = false;
			while (rs.next()) {

				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
				}
				result.add(row);

				writer.writeAll(rs, true);
				writer.close();

				dataPresence = true;
			}

			String[] text = { "No Data Present for " + currentDate };
			if (!dataPresence) {
				writer.writeNext(text, true);
				writer.close();

			}

			AtlasMailRequest atlasMailRequest = new AtlasMailRequest();

			subject = propMap.get("DAILY_ACTIVITY_REPORT_EMAIL_SUBJECT");
			Body = propMap.get("DAILY_ACTIVITY_REPORT_EMAIL_BODY");
//			fromMail = propMap.get("DAILY_ACTIVITY_REPORT_EMAIL_FROM");
//			toMail = propMap.get("DAILY_ACTIVITY_REPORT_EMAIL_TO");
//			ToMail = toMail.split(";");
//			ccmail = "avjadhav@ccrn.com;hsomwanshi@crosscountry.com;rkhimava@crosscountry.com";
//			ccMail = ccmail.split(";");
			//test
			fromMail = "amoshinde@crosscountry.com";
			toMail = "General - Stuti Test <18c163c3.crosscountry.com@amer.teams.ms>";
			ToMail = toMail.split(";");
			ccmail = "General - Stuti Test <18c163c3.crosscountry.com@amer.teams.ms>";
			ccMail = ccmail.split(";");

			atlasMailRequest.setTo(ToMail);
			atlasMailRequest.setCc(ccMail);
			atlasMailRequest.setFrom(fromMail);
			atlasMailRequest.setSubject(subject);
			atlasMailRequest.setBody(Body);
			atlasMailRequest.setAttachment("DailyActivityReport_" + currentDate + ".csv");

			atlasmailservice.sendEmail(atlasMailRequest);
			rs.close();

			// call the Daily Login logout function
			result = GetDailyReportLoginLogout(sdate2, edate2, propMap);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

}
