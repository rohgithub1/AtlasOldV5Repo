package com.cci.rest.service.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.AtlasGetUserMapping;


@Component
public class AtlasGetUserMappingsDao {

	@Autowired
	private DatabaseService databaseService;

	public List<AtlasGetUserMapping> getUserMapping(String user) throws Exception {

		List<AtlasGetUserMapping> list = new ArrayList<>();

		AtlasGetUserMapping userMap = new AtlasGetUserMapping();

		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();
			pstmt = mssqlconnection.prepareStatement(
					"select Mapping_data from user_process_Mapping where USERNAME = ? and is_active=1");
			pstmt.setString(1, user);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				userMap.setUserMapping(rs.getString("MAPPING_DATA"));
			}
			list.add(userMap);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			try {
				if(mssqlconnection!=null) {
					mssqlconnection.close();
				}if(pstmt!=null) {
					pstmt.close();
				}if(rs!=null) {
					rs.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		return list;
	}

}
