package com.cci.rest.service.dao;

import com.cci.rest.service.dataobjects.AtlasUsers;
import com.cci.rest.service.dataobjects.Atlas_ActionActivities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;

@Component
public class Atlas_ActionActivitiesDao {

	@Autowired
	private DatabaseService databaseSerive;

	public List<Atlas_ActionActivities> getAllActivities() {

		List<Atlas_ActionActivities> list = new ArrayList();
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			int k = 0;
			mssqlconnection = databaseSerive.getConnection();
			pstmt = mssqlconnection.prepareStatement("SELECT * FROM activities where IS_ACTIVE=1");
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				Atlas_ActionActivities activity = new Atlas_ActionActivities();

				if (rs.getString("ACTIVITY") == null) {

				} else {

					activity.setActivity(rs.getString("ACTIVITY"));

				}

				if (rs.getString("DESCRIPTION") == null) {

				} else {

					activity.setDescription(rs.getString("DESCRIPTION"));
				}

				if (rs.getString("IS_ACTIVE") == null) {

				} else {

					activity.setIsActive(Integer.parseInt(rs.getString("IS_ACTIVE")));
				}

				if (rs.getString("IS_ACTIVE") == null) {

				} else {

					activity.setIsActive(Integer.parseInt(rs.getString("IS_ACTIVE")));
				}

				if (rs.getString("CREATED_BY") == null) {

				} else {

					activity.setCreatedBy((rs.getString("CREATED_BY")));
				}

				if (rs.getString("CREATED_DATE") == null) {

				} else {

					activity.setCreatedDate((rs.getDate("CREATED_DATE")));
				}

				if (rs.getString("MODIFIED_BY") == null) {

				} else {

					activity.setModifiedBy((rs.getString("MODIFIED_BY")));
				}

				if (rs.getString("MODIFIED_DATE") == null) {

				} else {

					activity.setCreatedDate((rs.getDate("MODIFIED_DATE")));
				}

				list.add(k, activity);
				k = k + 1;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(mssqlconnection!=null) {
					mssqlconnection.close();
				}if(pstmt!=null) {
					pstmt.close();
				}if(rs!=null) {
					rs.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return list;

	}

	{

	}

}
