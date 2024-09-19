package com.cci.rest.service.dao;

import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.ProcessingDataAtlas;
import com.fasterxml.jackson.core.JsonParser;

import java.sql.Array;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * 
 * @author Rohit Khimavat
 *
 */
@Component
public class AtlasUpdateProcessingDataDao {

	@Autowired
	private DatabaseService databaseService;

	public List<ProcessingDataAtlas> updateDataProcessData(int id, String jsoncolumns, String userName) throws JSONException {

		String removestring = "{\"state\":";
		removestring = "{\"newState\":";

		String result = jsoncolumns.replace(removestring, "{updateState :");

		Map<String, String> inputValues = new HashMap<>();
		
		System.out.println("result "+result);
		
		JSONObject jobj = new JSONObject(result);
		JSONArray jarray = jobj.getJSONArray("updateState");
		try {
			
			for(int i=0; i<jarray.length();i++) {
				JSONObject insideJObj = jarray.getJSONObject(i);
				Iterator<String> k = insideJObj.keys();
					while(k.hasNext()) {
						String ke = k.next();
						System.out.println("Key Name :"+ke);
						String va = insideJObj.getString(ke);
						va = va.replace("~", ":");
						System.out.println("Value Name :"+va);
						inputValues.put(ke, va);
					}
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		int ID = id;
		String modifieddate = getEndDateTime();
		String userDomainName = userName;

		List<ProcessingDataAtlas> list = new ArrayList<>();

		String keyName = null;
		String keyValue = null;
		String updateQuery = "update revampAtlas_process_data set MODIFIED_BY = '" + userDomainName
				+ "', MODIFIED_DATE = '" + modifieddate + "', process_column_Data = JSON_MODIFY(JSON_MODIFY";
		String subUpdateQuery = "";

		for (Map.Entry<String, String> set : inputValues.entrySet()) {
			if (set.getKey().equals("processId") || set.getKey().equals("startTime") || set.getKey().equals("userName")
					|| set.getKey().equals("endTime") || set.getKey().equals("isActive")
					|| set.getKey().equals("createdBy") || set.getKey().equals("createdDate")
					|| set.getKey().equals("modifiedBy") || set.getKey().equals("modifiedDate")) {

			} else {
				keyName = set.getKey();
				if (keyName.length() > 0) {
					keyValue = inputValues.get(keyName);

					updateQuery = updateQuery + "(JSON_MODIFY";
					subUpdateQuery = subUpdateQuery + "'$.\"" + keyName + "\"','" + keyValue + "'),";
				}

			}
		}

		updateQuery = updateQuery + "(process_column_Data," + subUpdateQuery;
		updateQuery = updateQuery + "'$.modifiedBy','" + userDomainName + "'), '$.modifiedDate', '" + modifieddate
				+ "')";
		updateQuery = updateQuery + " where ID = " + ID;

		System.out.println(updateQuery);

		Connection mssqlconnection = databaseService.getConnection();

		try {

			Statement stmt = mssqlconnection.createStatement();

			stmt.execute(updateQuery);

			mssqlconnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;

	}

	public String getModifiedDateTime() {
		Date today = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		String endDateTime = df.format(today);

		return endDateTime;
	}

	public String getFormatedDateString(Date date) {
		String formatedDateString = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formatedDateString = sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return formatedDateString;
	}

	public Timestamp convertToDateTime(String date) {
		Timestamp datetime = null;
		Array d;

		try {
			Connection mssqlconnection = databaseService.getConnection();
			Statement stmt = mssqlconnection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT TRY_PARSE('" + date + "' AS DATETIME USING 'en-gb')");
			while (rs.next()) {
				datetime = rs.getTimestamp(1);
			}
			System.out.println(datetime);
			mssqlconnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datetime;
	}

	public String getEndDateTime() {
		Date today = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		String endDateTime = df.format(today);
		System.out.println(endDateTime);

		return endDateTime;
	}

	public String getDate() {
		Date today = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		String endDateTime = df.format(today);

		return endDateTime;
	}

}
