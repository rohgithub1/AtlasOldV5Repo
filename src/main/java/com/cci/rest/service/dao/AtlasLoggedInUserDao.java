package com.cci.rest.service.dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.AtlasUsers;
import com.cci.rest.service.dataobjects.Atlas_ProcessDataActivity;

@Component
public class AtlasLoggedInUserDao {

	@Autowired
	private AtlasUsersDao usersDao;

	@Autowired
	private DatabaseService databaseSerive;

	@Autowired
	private Atlas_DSProcessDataActivityDao DSProcessDataActivityDao;

	String IN_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	String OUT_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
	SimpleDateFormat insdf = new SimpleDateFormat(IN_DATE_FORMAT);
	SimpleDateFormat outsdf = new SimpleDateFormat(OUT_DATE_FORMAT);

	Atlas_ProcessDataActivity processDataActivity = new Atlas_ProcessDataActivity();
	Atlas_ProcessDataActivity processDataActivity_loginActivity = new Atlas_ProcessDataActivity();

//   All Details of user after Logging In

	public List<AtlasUsers> getUserDetails(AtlasUsers user) throws SQLException {

		List<AtlasUsers> list = new ArrayList();
		
		Connection mssqlconnection = null;
		Statement stmt = null;
		try {
			mssqlconnection = databaseSerive.getConnection();
			stmt = mssqlconnection.createStatement();

			if (user.isAutheticateUser()) {
				ResultSet rs = stmt.executeQuery("Select * from users where DOMAIN_LOGIN_NAME ='"
						+ user.getDomainLoginName().trim() + "' and IS_ACTIVE= 1");

				int k = 0;
				while (rs.next()) {

					if (rs.getString("DOMAIN_LOGIN_NAME") == null) {

					} else {
						user.setDomainLoginName(rs.getString("DOMAIN_LOGIN_NAME").trim());
					}

					if (rs.getString("FIRST_NAME") == null) {

					} else {

						user.setFirstName(rs.getString("FIRST_NAME").trim());
					}

					if (rs.getString("LAST_NAME") == null) {

					} else {

						user.setLastName(rs.getString("LAST_NAME").trim());
					}

					if (rs.getString("ID") == null) {

					} else {
						user.setUserId(Integer.parseInt(rs.getString("ID")));
					}

				}

				list.add(k, user);
				k = k + 1;

				if (list.isEmpty()) {
					list.add(0, user);
				}

//        		--------------------Getting Initial Activity Time-----------

				String loginUser = user.getDomainLoginName();
				String activityStartTime = null;
				String activityEndTime = null;
				long diffHours = 0;
				Date System_Time = null;
				Date Start_Time = null;
				Date endTime = null;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OUT_DATE_FORMAT);

				Instant timeStamp = Instant.now();
				ZonedDateTime LAZone = timeStamp.atZone(ZoneId.of("America/New_York"));
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(OUT_DATE_FORMAT);

				String Current_TimeEST = LAZone.format(formatter);

				Date Current_Time = simpleDateFormat.parse(Current_TimeEST);

				Date currentSystemTime = simpleDateFormat.parse(Current_TimeEST);

				System_Time = currentSystemTime;

				ArrayList<String> activityTime = new ArrayList<String>();

				activityTime = DSProcessDataActivityDao.selectStartTimeOfActivity(loginUser);

				if (activityTime.size() > 0 && activityTime.get(0) != null) {
					activityStartTime = activityTime.get(0);

					activityEndTime = activityTime.get(1);

					if (activityStartTime.length() > 0 || !activityStartTime.isEmpty()) {

						DateFormat targetFormat = new SimpleDateFormat(IN_DATE_FORMAT);
						Start_Time = targetFormat.parse(activityStartTime);

						long diff = System_Time.getTime() - Start_Time.getTime();
						long diffSeconds = diff / 1000 % 60;
						long diffMinutes = diff / (60 * 1000) % 60;
						diffHours = diff / (60 * 60 * 1000) % 24;
						String totalDiff = diffHours + ":" + diffMinutes + ":" + diffSeconds;

						if (activityEndTime == null || activityEndTime.isEmpty()) {

							if ((int) diffHours > 1) {
//								Adding 60 minutes to Start_Time
								SimpleDateFormat insdf24hrs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String formattedStartTime = insdf24hrs.format(Start_Time);
								// System.out.println( "formattedStartTime "+ formattedStartTime );
								DateTimeFormatter informatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
								LocalDateTime ldt = LocalDateTime.parse(formattedStartTime, informatter);
								ldt = ldt.plusSeconds(3600);
								Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
								endTime = Date.from(instant);
								// System.out.println( "EndTime "+ endTime);

							} else {
								endTime = System_Time;
								// System.out.println( "EndTime "+ endTime);
							}
						} else {
							endTime = targetFormat.parse(activityEndTime);
							// System.out.println( "EndTime "+ endTime);
						}

					} else {
//						DOUBT IN THIS SECTION

						endTime = Current_Time;
						processDataActivity.setStartTime(Current_Time);
						processDataActivity.setCreatedDate(Current_Time);
					}
				}

				// To update last activity having end time = null

				processDataActivity.setEndTime(endTime);
				processDataActivity.setCreatedBy(user.getDomainLoginName());
				processDataActivity.setModifiedBy(user.getDomainLoginName());
				processDataActivity.setModifiedDate(Current_Time);
				// System.out.println("getModifiedDate : " +
				// processDataActivity.getModifiedDate());

				// To insert login activity
//				System.out.print("On Inserting login Activity");
				processDataActivity_loginActivity.setStartTime(Current_Time);
//				System.out.println("getStartTime : " + processDataActivity.getStartTime());
				processDataActivity_loginActivity.setEndTime(System_Time);
//				System.out.println("getEndTime : " + processDataActivity.getEndTime());
				processDataActivity_loginActivity.setCreatedBy(user.getDomainLoginName());
				processDataActivity_loginActivity.setActivityName("Login");
				processDataActivity_loginActivity.setComments("Login");
				processDataActivity_loginActivity.setIsActive(1);
				processDataActivity_loginActivity.setCreatedDate(Current_Time);
				processDataActivity_loginActivity.setModifiedBy(user.getDomainLoginName());
				processDataActivity_loginActivity.setModifiedDate(Current_Time);

				DSProcessDataActivityDao.updateProcessDataActivity(processDataActivity);

				DSProcessDataActivityDao.insertProcessDataActivity(processDataActivity_loginActivity);

				user.setModifiedBy(user.getDomainLoginName());
				user.setModifiedDate(Current_Time);
				user.setLastLogin(Current_Time);

				usersDao.UpdateLastLogin(user);

//		  		END OF BLOCK
			} else {
				list.add(0, user);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mssqlconnection != null) {
				mssqlconnection.close();
			}
			if(stmt!=null) {
				stmt.close();
			}
		}
		return list;
	}

}
