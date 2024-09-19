package com.cci.rest.service.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.AtlasActivitiesName;


@Component
public class AtlasActivitiesNameDao {

	@Autowired
	private DatabaseService databaseSerive;

	public List<AtlasActivitiesName> getActivitiesName() throws SQLException {

		List<AtlasActivitiesName> list = new ArrayList();
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			int k = 0;
			int sno = 1;
			mssqlconnection = databaseSerive.getConnection();
			pstmt = mssqlconnection.prepareStatement("select ACTIVITY_NAME,DESCRIPTION from activity_name where IS_ACTIVE = 1");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				AtlasActivitiesName activity = new AtlasActivitiesName();

				if (rs.getString("ACTIVITY_NAME").toString() == null) {

				} else {
					activity.setActivityName(rs.getString("ACTIVITY_NAME"));
					activity.setDescription(rs.getString("DESCRIPTION"));
					activity.setSno(sno);
				}

				list.add(k, activity);
				k = k + 1;
				sno = sno + 1;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(mssqlconnection!=null) {
				mssqlconnection.close();
			}if(pstmt!=null) {
				pstmt.close();
			}if(rs!=null) {
				rs.close();
			}
		}

		return list;

	}

	{

	}

}
