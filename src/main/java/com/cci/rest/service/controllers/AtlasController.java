package com.cci.rest.service.controllers;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.cci.rest.service.Authenticate.LoginAuthenticator;
import com.cci.rest.service.Scheduler.Scheduler;
import com.cci.rest.service.dao.AtlasActivitiesNameDao;
import com.cci.rest.service.dao.AtlasGetProcessDataDAO;
import com.cci.rest.service.dao.AtlasGetUserMappingsDao;
import com.cci.rest.service.dao.AtlasInsertProcessingDataDao;
import com.cci.rest.service.dao.AtlasLoggedInUserDao;
import com.cci.rest.service.dao.AtlasMinimumCommentLengthDao;
import com.cci.rest.service.dao.AtlasProcessNameDao;
import com.cci.rest.service.dao.AtlasReportTypesDAO;
import com.cci.rest.service.dao.AtlasUpdateProcessingDataDao;
import com.cci.rest.service.dao.Atlas_ActionActivitiesDao;
import com.cci.rest.service.dao.Atlas_DSProcessDataActivityDao;
import com.cci.rest.service.dao.Atlas_ProcessesAssignedToUser;
import com.cci.rest.service.dao.Dashboard_ActivityDAO;
import com.cci.rest.service.dao.MappingDataDao;
import com.cci.rest.service.dao.SendEmailDao;
import com.cci.rest.service.dataobjects.ActiveProcessList;
import com.cci.rest.service.dataobjects.AtlasActivitiesName;
import com.cci.rest.service.dataobjects.AtlasGetUserMapping;
import com.cci.rest.service.dataobjects.AtlasUsers;
import com.cci.rest.service.dataobjects.Atlas_ActionActivities;
import com.cci.rest.service.dataobjects.Atlas_ProcessDataActivity;
import com.cci.rest.service.dataobjects.ProcessingDataAtlas;
import com.cci.rest.service.dataobjects.ProcessnameAtlas;
import com.cci.rest.service.dataobjects.UserProcessDetailsAtlas;
import com.cci.rest.service.mail.AtlasMailRequest;
import com.cci.rest.service.mail.AtlasMailService;
import com.opencsv.CSVWriter;
import com.cci.rest.service.dataobjects.AtlasMinimumCommentLength;
import com.cci.rest.service.dataobjects.SendEmail;

@CrossOrigin(origins = "*")
@RepositoryRestController
@RequestMapping({ "/atlas" })
public class AtlasController {

	private String fromMail = "";
	private String toMail = "";
	private String ccmail = "";
	private String Body;
	String[] ccMail, ToMail = null;
	private String subject = "";

	@Autowired
	JavaMailSender javamailsender;

	@Autowired
	Dashboard_ActivityDAO dashboardActivityDAO;

	@Autowired
	AtlasMailService atlasmailservice;

	@Autowired
	MappingDataDao mappingdataDAO;

	@Autowired
	AtlasLoggedInUserDao atlasUserDao;

	@Autowired
	Atlas_ActionActivitiesDao actionActivitiesDao;

	@Autowired
	AtlasActivitiesNameDao activitiesNameDao;

	@Autowired
	Atlas_DSProcessDataActivityDao processDataActivityDao;

	@Autowired
	Atlas_ProcessDataActivity processDataActivity;

	@Autowired
	Atlas_ProcessDataActivity productionprocessDataActivity;

	@Autowired
	LoginAuthenticator loginautheticator;

	@Autowired
	Atlas_ProcessesAssignedToUser atlasprocessDAO;

	@Autowired
	AtlasProcessNameDao atlasprocessnameDAO;

	@Autowired
	AtlasInsertProcessingDataDao insertAtlasProcessingDataDao;

	@Autowired
	AtlasGetProcessDataDAO selectatlasprocessdao;

	@Autowired
	AtlasReportTypesDAO atlasReportDataDao;

	@Autowired
	AtlasUpdateProcessingDataDao updateAtlasProcessingDataDao;

	@Autowired
	AtlasMinimumCommentLengthDao minCommentLengthDao;

	@Autowired
	AtlasGetUserMappingsDao atlasGetUserMap;

	@Autowired
	SendEmailDao sendemaildao;

	@Autowired
	Scheduler scheduler;

	public String username;

	@RequestMapping(value = {
			"/LoginAtlas" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<AtlasUsers>> Login(@RequestBody Map<String, String> user) throws JSONException {
		System.out.println("Login Starts");
		Map<String, String> inputValues = new HashMap<>();
		String sessionbody = user.get("body");

		JSONObject jobj = new JSONObject(sessionbody);
		username = jobj.getString("username");

		String password = jobj.getString("password");
//		username = user.get("username");  for amol
//		
//		String password = user.get("password");
//		System.out.println("Password :" + password);
		List<AtlasUsers> users = null;
		AtlasUsers userobj = new AtlasUsers();
		try {
			if (!((password == null) || (password.trim().isEmpty()))) {
				userobj = loginautheticator.authenticateUser(username, password);

				users = atlasUserDao.getUserDetails(userobj);

			} else {
				userobj.setAutheticateUser(false);
				users = atlasUserDao.getUserDetails(userobj);
			}

			return new ResponseEntity<List<AtlasUsers>>(users, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<List<AtlasUsers>>(users, HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = {
			"/SendEmail" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<SendEmail>> SendEmail(@RequestBody Map<String, String> userDomainId) {

		String userId;

		if (userDomainId != null) {
			System.out.println(userDomainId);

			Map<String, String> inputValues = new HashMap<>();
			String sessionbody = userDomainId.get("body");
			String[] sessionData = sessionbody.split(",");
			for (int i = 0; i < sessionData.length; i++) {
				String[] sessionDataAaray = sessionData[i].split(":");

				String key = sessionDataAaray[0].replace('{', ' ');
				key = key.replace('"', ' ');

				String value = sessionDataAaray[1].replace('"', ' ');
				value = value.replace('}', ' ');
				inputValues.put(key.trim(), value.trim());
			}

			userId = (String) inputValues.get("username");
			System.out.println("user Id in body " + userId);
		} else {

			userId = username;
		}

		List<SendEmail> getManagerInfo = null;
		List<String> setMessage = null;

		AtlasMailRequest atlasMailRequest = new AtlasMailRequest();

		boolean mailSent = false;

		try {

			getManagerInfo = sendemaildao.getManagerDetails(userId);
			System.out.println(getManagerInfo);

			System.out.println(getManagerInfo.get(0).getManagerName());

			if (getManagerInfo.get(0).getManagerName() != null) {

				subject = "Atlas user can't SignIn !!";
				Body = "Unable to login the user having username " + '"' + userId + '"' + " into Atlas Application.";
				fromMail = "noreply@crosscountry.com";
				toMail = "avjadhav@ccrn.com;hsomwanshi@crosscountry.com;rkhimava@ccrn.com";
				ToMail = toMail.split(";");
				ccmail = "rkhimava@ccrn.com";
				ccMail = ccmail.split(";");

				atlasMailRequest.setTo(ToMail);
				atlasMailRequest.setCc(ccMail);
				atlasMailRequest.setFrom(fromMail);
				atlasMailRequest.setSubject(subject);
				atlasMailRequest.setBody(Body);

				atlasmailservice.sendEmail(atlasMailRequest);

				mailSent = true;
			}

			return new ResponseEntity<List<SendEmail>>(getManagerInfo, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<List<SendEmail>>(HttpStatus.UNAUTHORIZED);
		}

	}

	@RequestMapping(value = {
			"/ActionActivities" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Atlas_ActionActivities>> getAllActivities() {
		System.out.println("Activities Starts");
		List<Atlas_ActionActivities> activities = null;

		try {
			activities = actionActivitiesDao.getAllActivities();

			if (activities.size() > 0) {

				return new ResponseEntity<List<Atlas_ActionActivities>>(activities, HttpStatus.OK);

			} else {
				return new ResponseEntity<List<Atlas_ActionActivities>>(activities, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<List<Atlas_ActionActivities>>(activities, HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = {
			"/ActivitiesName" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<AtlasActivitiesName>> getActivitiesName() {
		System.out.println("Activities Name Starts");
		List<AtlasActivitiesName> activitiesName = null;

		try {
			activitiesName = activitiesNameDao.getActivitiesName();

			if (activitiesName.size() > 0) {

				return new ResponseEntity<List<AtlasActivitiesName>>(activitiesName, HttpStatus.OK);

			} else {
				return new ResponseEntity<List<AtlasActivitiesName>>(activitiesName, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<List<AtlasActivitiesName>>(activitiesName, HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = {
			"/MinCommentLength" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<AtlasMinimumCommentLength>> getCommentMinLength() {
		System.out.println("Min Comment Length Service Starts");
		List<AtlasMinimumCommentLength> commentLength = null;

		try {
			commentLength = minCommentLengthDao.getCommentMinLength();
//			System.out.println(commentLength);

			if (commentLength.size() > 0) {

				return new ResponseEntity<List<AtlasMinimumCommentLength>>(commentLength, HttpStatus.OK);

			} else {
				return new ResponseEntity<List<AtlasMinimumCommentLength>>(commentLength, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<List<AtlasMinimumCommentLength>>(commentLength, HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = {
			"/ProcessDataActivities" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Atlas_ProcessDataActivity> processDataActivity(@RequestBody Map<String, String> session) {

		boolean updateResult = false;
		boolean insertResult = false;

		try {

			Map<String, String> inputValues = new HashMap<>();
			String sessionbody = session.get("body");

			String[] sessionData = sessionbody.split(",\"");

			for (int i = 0; i < sessionData.length; i++) {
				String[] sessionDataAaray = sessionData[i].split("\":");

				String key = sessionDataAaray[0].replace('{', ' ');
				key = key.replace('"', ' ');

				String value = sessionDataAaray[1].replace('"', ' ');
				value = value.replace('}', ' ');
				inputValues.put(key.trim(), value.trim());
			}

			String processId = (String) inputValues.get("Current_ProcessID");
			String comments = (String) inputValues.get("comments");
			String activityName = (String) inputValues.get("Activity_Name");
//			String productionActivityActive = (String) inputValues.get("Production_Activity");
			String userName = (String) inputValues.get("userName");
//			System.out.println("data : " + processId +" "+ comments +" "+ activityName +" "+ userName);

			int currentProcessId = Integer.parseInt(processId);

			String currentUser = userName;

			System.out.println("currentUser : " + currentUser);

			productionprocessDataActivity = processDataActivityDao
					.populateUpdateProcessDataActivityObject(currentProcessId, currentUser, activityName);

			updateResult = processDataActivityDao.updateProcessDataActivity(productionprocessDataActivity);

			processDataActivity = processDataActivityDao.populateProcessDataActivityObject(currentProcessId,
					currentUser, activityName, comments);

			insertResult = processDataActivityDao.insertProcessDataActivity(processDataActivity);

//			System.out.println(processDataActivity);

			if (updateResult || insertResult == true) {

				// mailservice.sendEmail(mailRequest);
//				System.out.println(HttpStatus.OK);
				return new ResponseEntity<Atlas_ProcessDataActivity>(HttpStatus.OK);

			} else {
//				System.out.println(HttpStatus.NO_CONTENT);
				return new ResponseEntity<Atlas_ProcessDataActivity>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
//			e.printStackTrace();
			return new ResponseEntity<Atlas_ProcessDataActivity>(HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = {
			"/AtlasLogout" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> Logout() {
		System.out.println("Logout Starts");
		Date endTime = null;
		String OUT_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OUT_DATE_FORMAT);
		Atlas_ProcessDataActivity processDataActivity = new Atlas_ProcessDataActivity();

		Instant timeStamp = Instant.now();
		ZonedDateTime LAZone = timeStamp.atZone(ZoneId.of("America/New_York"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(OUT_DATE_FORMAT);

		try {

			if (username != null || username.length() != 0) {

				String Current_TimeEST = LAZone.format(formatter);
				Date CurrentTime = simpleDateFormat.parse(Current_TimeEST);

				endTime = CurrentTime;

				processDataActivity.setCreatedBy(username);
				processDataActivity.setModifiedBy(username);
				processDataActivity.setEndTime(endTime);
				processDataActivity.setModifiedDate(endTime);

				processDataActivityDao.updateProcessDataActivity(processDataActivity);

				processDataActivity.setActivityName("Logout");
				processDataActivity.setComments("Logout");
				processDataActivity.setIsActive(1);
				processDataActivity.setStartTime(endTime);
				processDataActivity.setCreatedDate(endTime);
				processDataActivity.setModifiedDate(endTime);
				processDataActivity.setProcessId(0);

				processDataActivityDao.insertProcessDataActivity(processDataActivity);

				return new ResponseEntity<String>(HttpStatus.OK);

			} else {
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
		}
	}

	/*
	 * Get Process name based on userId
	 */

	@RequestMapping(value = {
			"/GetProcessDetails" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<UserProcessDetailsAtlas>> processesAssignedToUser(
			@RequestBody Map<String, String> inputparam) throws JSONException {

		String sessionbody = inputparam.get("body");
//		String[] sessionData = sessionbody.split(",");
//		for (int i = 0; i < sessionData.length; i++) {
//			String[] sessionDataAaray = sessionData[i].split(":");
//
//			String key = sessionDataAaray[0].replace('{', ' ');
//			key = key.replace('"', ' ');
//
//			String value = sessionDataAaray[1].replace('"', ' ');
//			value = value.replace('}', ' ');
//			inputValues.put(key.trim(), value.trim());
//		}
//
//		String username = (String) inputValues.get("username");

		JSONObject jobj = new JSONObject(sessionbody);
		String username = jobj.getString("username");
		System.out.println(username);
		List<UserProcessDetailsAtlas> data = null;
		try {
			data = atlasprocessDAO.processesAssignedToUser(username);
			return new ResponseEntity<List<UserProcessDetailsAtlas>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<UserProcessDetailsAtlas>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = {
			"/GetProcessIdFromProcessName" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<ProcessnameAtlas>> GetProcessIdFromProcessName(
			@RequestBody Map<String, String> inputparam) throws JSONException {

		System.out.println("ProcessIDFromProcessName Starts");

//		Map<String, String> inputValues = new HashMap<>();
		String sessionbody = inputparam.get("body");
		JSONObject jobj = new JSONObject(sessionbody);

//		String[] sessionData = sessionbody.split(",");
//		for (int i = 0; i < sessionData.length; i++) {
//			String[] sessionDataAaray = sessionData[i].split(":");
//
//			String key = sessionDataAaray[0].replace('{', ' ');
//			key = key.replace('"', ' ');
//
//			String value = sessionDataAaray[1].replace('"', ' ');
//			value = value.replace('}', ' ');
//			inputValues.put(key.trim(), value.trim());
//		}

//		String pName = (String) inputValues.get("pName");
		String pName = jobj.getString("pName");

		List<ProcessnameAtlas> processNameId = null;
		try {
			processNameId = atlasprocessnameDAO.getProcessIdForProcessName(pName);

			return new ResponseEntity<List<ProcessnameAtlas>>(processNameId, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<ProcessnameAtlas>>(processNameId, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@RequestMapping(value = {
			"/InsertDataProcessData" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<ProcessingDataAtlas>> InsertDataProcessData(
			@RequestBody Map<String, String> inputparam) {

		String insertbody = inputparam.get("body");
		System.out.println("Insert Record start " + insertbody);

		List<ProcessingDataAtlas> processJsonData = null;
		try {
			insertAtlasProcessingDataDao.insertDataProcessData(insertbody);
			return new ResponseEntity<List<ProcessingDataAtlas>>(processJsonData, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<List<ProcessingDataAtlas>>(processJsonData, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = {
			"/GetProcessColumnNameAsJSON" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<LinkedHashMap<String, String>> GetProcessColumnNameAsJSON(
			@RequestBody ProcessingDataAtlas inputparam) {
		System.out.println("Param" + inputparam);
		int processid = inputparam.getProcessId();

		LinkedHashMap<String, String> processJsonData = null;
		try {
			processJsonData = atlasprocessDAO.getColumnNameForProcess(processid);

			return new ResponseEntity<LinkedHashMap<String, String>>(processJsonData, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<LinkedHashMap<String, String>>(processJsonData, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	/*
	 * Fetch data for selected process along with the column name for first time
	 */

	@RequestMapping(value = {
			"/GetProcessDataAsJSON" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetProcessDataAsJSON(@RequestBody Map<String, String> inputparam) {

		int processId = Integer.parseInt(inputparam.get("processId"));
		int limit = Integer.parseInt(inputparam.get("pageSize"));
		List<Map<String, Object>> data = null;
		try {
			data = selectatlasprocessdao.getProcessDataAsJSON(processId, limit);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Fetch the new data after 1st attempt and only fetch the limited data
	 */
	@RequestMapping(value = {
			"/GetNewProcessDataAsJSON" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetNewProcessDataAsJSON(
			@RequestBody Map<String, String> inputparam) {

		String insertbody = inputparam.get("body");
		System.out.println("NewProcessData " + insertbody);

		List<Map<String, Object>> data = null;
		try {
			data = selectatlasprocessdao.getNewProcessDataAsJson(insertbody);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Controller used to get the review data process wise and user wise
	 */

	@RequestMapping(value = {
			"/GetProcessReviewDataForUser" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetProcessReviewDataForUser(
			@RequestBody Map<String, String> inputparam) {

		int processId = Integer.parseInt(inputparam.get("processId"));
		System.out.println(processId);
		String userName = inputparam.get("userName");
		List<Map<String, Object>> data = null;
		try {
			data = selectatlasprocessdao.getProcessReviewDataForUser(processId, userName);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Controller used to get the process wise report data for selected user
	 */

	@RequestMapping(value = {
			"/GetReportDataForUser" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetReportDataForUser(@RequestBody Map<String, String> inputparam) {

		// Map<String, String> inputValues = new HashMap<>();

		int processId = Integer.parseInt(inputparam.get("processId"));
		String userName = inputparam.get("userName");
		String startDate = inputparam.get("startDate");
		System.out.println(startDate);
		String endDate = inputparam.get("endDate");
		System.out.println(endDate);

		System.out.println(processId + " " + startDate + "" + endDate);

		List<Map<String, Object>> data = null;
		try {
			data = atlasReportDataDao.reportDumpProcessData(processId, startDate, endDate, userName);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Controller used to get the process wise report data for selected user
	 */

	@RequestMapping(value = {
			"/GetProcessWiseReportAllUser" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetProcessWiseReportAllUser(
			@RequestBody Map<String, String> inputparam) {

		// Map<String, String> inputValues = new HashMap<>();

		int processId = Integer.parseInt(inputparam.get("processId"));
		String userName = inputparam.get("userName");
		String startDate = inputparam.get("startDate");

		String endDate = inputparam.get("endDate");

		System.out.println(processId + " " + startDate + "" + endDate);

		List<Map<String, Object>> data = null;
		try {
			data = atlasReportDataDao.reportDumpProcessDataAllUser(processId, startDate, endDate, userName);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Controller used to get activity report using userid
	 */

	@RequestMapping(value = {
			"/GetActivityReportData" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetActivityReportData(
			@RequestBody Map<String, String> inputparam) {

		// Map<String, String> inputValues = new HashMap<>();

		String userName = inputparam.get("userName");
		String startDate = inputparam.get("startDate");
//		System.out.println(startDate);
		String endDate = inputparam.get("endDate");
//		System.out.println(endDate);

		System.out.println(userName + " " + startDate + "" + endDate);

		List<Map<String, Object>> data = null;
		try {
			data = atlasReportDataDao.reportActivityDataUserwise(userName, startDate, endDate);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Controller used to get activity report for all users
	 */

	@RequestMapping(value = {
			"/GetActivityReportDataAllUser" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetActivityReportDataAllUser(
			@RequestBody Map<String, String> inputparam) {

		// Map<String, String> inputValues = new HashMap<>();

		String userName = inputparam.get("userName");
		String startDate = inputparam.get("startDate");
//		System.out.println(startDate);
		String endDate = inputparam.get("endDate");
//		System.out.println(endDate);

		System.out.println(userName + " " + startDate + "" + endDate);

		List<Map<String, Object>> data = null;
		try {
			data = atlasReportDataDao.reportActivityDataAllUser(startDate, endDate);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Controller for updating process data
	 */

	@RequestMapping(value = {
			"/UpdateDataProcessData" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<ProcessingDataAtlas>> UpdateDataProcessData(@RequestBody Map<Object, Object> inputparam)
			throws JSONException {

		System.out.println(inputparam);
		String body = inputparam.get("body").toString();

		JSONObject obj = new JSONObject(body);

		int id = Integer.parseInt(obj.getString("id"));
		String jsoncolumns = obj.getString("jsonColumns");
		String userName = obj.getString("userName");

		System.out.println(id + jsoncolumns + userName);

		List<ProcessingDataAtlas> processJsonData = null;
		try {
			updateAtlasProcessingDataDao.updateDataProcessData(id, jsoncolumns, userName);

			return new ResponseEntity<List<ProcessingDataAtlas>>(processJsonData, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<List<ProcessingDataAtlas>>(processJsonData, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Get list of all active users
	 */

	@RequestMapping(value = {
			"/GetAllActiveUsers" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<ProcessnameAtlas>> GetAllActiveUsers() throws JSONException {

		List<ProcessnameAtlas> activeUsers = null;
		try {
			activeUsers = selectatlasprocessdao.getAllActiveUsers();
			System.out.println("activeUsers : " + activeUsers);
			if (activeUsers.size() > 0) {
				return new ResponseEntity<List<ProcessnameAtlas>>(activeUsers, HttpStatus.OK);
			} else {
				return new ResponseEntity<List<ProcessnameAtlas>>(activeUsers, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<List<ProcessnameAtlas>>(activeUsers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Add new process or update any process
	 */

	@RequestMapping(value = {
			"/AtlasProcessActions" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> AddNewProcess(@RequestBody Map<String, String> inputparam) {
		try {
			boolean result = false;
			System.out.println("inputparam.get(\"body\") " + inputparam.get("userName"));

			Map<String, String> inputValues = new HashMap<>();
			System.out.println("inputparam: " + inputparam);

			String sessionbody = inputparam.get("body");
			sessionbody = sessionbody.replace("}\"", "}}");
			System.out.println("SessionBody --- " + sessionbody);

			String[] sessionData = sessionbody.split(",");

			String insertbody = inputparam.get("body");

			result = insertAtlasProcessingDataDao.insertNewProcess(insertbody);
			if (result) {
				return new ResponseEntity<String>(HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
		}

	}

	/*
	 * Get list of all active processes
	 */

	@RequestMapping(value = {
			"/GetAllActiveProcess" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<ActiveProcessList>> GetAllActiveProcess(@RequestBody Map<Object, Object> inputparam)
			throws JSONException {

		System.out.println("inputparam : " + inputparam);

		List<ActiveProcessList> activeProcess = null;
		try {
			activeProcess = selectatlasprocessdao.getAllActiveProcesses();
			System.out.println("activeProcess : " + activeProcess);
			if (activeProcess.size() > 0) {
				return new ResponseEntity<List<ActiveProcessList>>(activeProcess, HttpStatus.OK);
			} else {
				return new ResponseEntity<List<ActiveProcessList>>(activeProcess, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<List<ActiveProcessList>>(activeProcess, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Process Mapping Actions
	 */

	@RequestMapping(value = {
			"/GetAllProcessMappingAction" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> GetAllProcessMappingAction(@RequestBody Map<Object, Object> inputparam)
			throws JSONException {

		System.out.println("inputparam : " + inputparam);
		boolean processmappingactionresult = false;
		String insertbody = inputparam.get("body").toString();

		try {
			processmappingactionresult = insertAtlasProcessingDataDao.procesMappingActions(insertbody);
			if (processmappingactionresult) {
				return new ResponseEntity<String>(HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
		}
	}

	/*
	 * Process Mapping Actions updated
	 */

	@RequestMapping(value = {
			"/GetUserProcessMappingAction" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> GetUserProcessMappingAction(@RequestBody Map<Object, Object> inputparam)
			throws JSONException {

		System.out.println("inputparam : " + inputparam);
		boolean processmappingactionresult = false;
		String body = inputparam.get("body").toString();

		JSONObject obj = new JSONObject(body);

		String selectedUserName = obj.getString("selectedUser");
		String userProcessMappingData = obj.getString("processName");
		String userName = obj.getString("userName");

		System.out.println(selectedUserName + userProcessMappingData + userName);

		List<ProcessingDataAtlas> processJsonData = null;
		try {
			processmappingactionresult = insertAtlasProcessingDataDao.userProcesMappingActions(selectedUserName,
					userProcessMappingData, userName);

			if (processmappingactionresult) {
				return new ResponseEntity<String>(HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = {
			"/GetUserMappings" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<AtlasGetUserMapping>> getUserMapping(@RequestBody Map<String, String> session) {

		Map<String, String> inputValues = new HashMap<>();
		String sessionbody = session.get("body");
		String[] sessionData = sessionbody.split(",");
		for (int i = 0; i < sessionData.length; i++) {
			String[] sessionDataAaray = sessionData[i].split(":");

			String key = sessionDataAaray[0].replace('{', ' ');
			key = key.replace('"', ' ');

			String value = sessionDataAaray[1].replace('"', ' ');
			value = value.replace('}', ' ');
			inputValues.put(key.trim(), value.trim());
		}

		String user = (String) inputValues.get("selectedUser");

		System.out.println(user);

		List<AtlasGetUserMapping> result = null;

		try {
			result = atlasGetUserMap.getUserMapping(user);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			if (result != null) {

				// mailservice.sendEmail(mailRequest);
				return new ResponseEntity<List<AtlasGetUserMapping>>(result, HttpStatus.OK);

			} else {
				return new ResponseEntity<List<AtlasGetUserMapping>>(HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			return new ResponseEntity<List<AtlasGetUserMapping>>(HttpStatus.UNAUTHORIZED);
		}

	}

//	to test the scheduler
	@RequestMapping(value = {
			"/test" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> getDailyActivityReport() throws JSONException {

		List<Map<String, Object>> res = null;
		try {

			res = scheduler.getDailyActivityReport();
			return new ResponseEntity<List<Map<String, Object>>>(res, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(HttpStatus.UNAUTHORIZED);
		}
	}

	/*
	 * controller used for get Login-Logout Report currently used --created
	 */
	@RequestMapping(value = "/GetLoginLogoutReportAll", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetLoginLogoutReport(@RequestBody Map<String, String> inputparam) {
		System.out.println("in GetLoginLogoutReport ");
		String startDate = inputparam.get("startDate");
		String endDate = inputparam.get("endDate");

		System.out.println(startDate + ":" + endDate);

		List<Map<String, Object>> data = null;
		try {
			data = atlasReportDataDao.GetLoginLogoutReport(startDate, endDate);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * controller used for get All Process report currently used --created
	 */
	@RequestMapping(value = "/GetAllProcessReport", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetAllProcessReport(@RequestBody Map<String, String> inputparam) {
		System.out.println("in get all ");
		List<ActiveProcessList> alist = new ArrayList<>();
		String startDate = inputparam.get("startDate");
		String endDate = inputparam.get("endDate");

		System.out.println(startDate + ":" + endDate);

		List<Map<String, Object>> data = null;
		try {
			// get all processes
			alist = atlasprocessDAO.getAllProcessName();
			data = processDataActivityDao.processDataActivityObject(startDate, endDate, alist);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * controller used for get All Process report and alloted resources --created
	 */
	@RequestMapping(value = "/GetAllProcessWithCount", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetAllProcessWithCount(
			@RequestBody Map<String, String> inputparam) {
		System.out.println("in GetAllProcessWithCount");
		String startDate = inputparam.get("startDate");
		String endDate = inputparam.get("endDate");

		System.out.println(startDate + ":" + endDate);

		List<Map<String, Object>> data = null;
		try {			
			data = insertAtlasProcessingDataDao.getRevampProcessDataActivityObject(startDate, endDate);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * controller used to get the Overall Queue view report Created --Rohit Khimavat
	 */

	@RequestMapping(value = "/GetOverAllQueueViewDashboardData", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> GetOverAllQueueViewDashboardData(
			@RequestBody Map<String, String> inputparam) throws JSONException {
		System.out.println("Fetching the queue wise dashboard data");
		// new
		String body = inputparam.get("body").toString();
		JSONObject obj = new JSONObject(body);
		String startDate = obj.getString("startDate");
		String endDate = obj.getString("endDate");

		
		System.out.println(startDate + " " + endDate);
		List<Map<String, Object>> data = null;
		try {
			data = dashboardActivityDAO.getOverAllQueueViewdashboardData(startDate, endDate);
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Map<String, Object>>>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * controller used for get All Activity
	 *  currently used  --created
	 */
	@RequestMapping(value = {
		"/GetAllActivityByProcId" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
			public ResponseEntity<List<Map<String, Object>>> getActivityByProcessId(
					@RequestBody Map<String, String> inputparam) throws JSONException {
			System.out.println("Fetching the agent wise dashboard data");
			System.out.println(inputparam);
			
			//while calling from frontEnd
			String body = inputparam.get("body").toString();
			JSONObject obj = new JSONObject(body);
			String startDate = obj.getString("startDate");
			String endDate = obj.getString("endDate");
						
			String startTime = startDate + " 00:00:00";
			String endTime = endDate + " 23:59:59";
			
			System.out.println("startTime = "+startTime+" endTime - "+endTime);
			
			List<Map<String, Object>> result = null;
			
			try {
				//first find all users login logout data
				result = dashboardActivityDAO.getProcessDataActivityObject(startTime,endTime);
				return new ResponseEntity<List<Map<String, Object>>>(result, HttpStatus.OK);
			
			} catch (Exception e) {
				return new ResponseEntity<List<Map<String, Object>>>(HttpStatus.UNAUTHORIZED);
			}
		}

	
}
