package com.cci.rest.service.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.ProcessnameAtlas;


@Component
public class AtlasProcessNameDao {
	
	@Autowired
	private DatabaseService databaseService;


public List<ProcessnameAtlas> getProcessIdForProcessName(String processName) throws SQLException{
		
		List<ProcessnameAtlas> list = new ArrayList<>();
		
		int k= 0;
		
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();
			pstmt = mssqlconnection.prepareStatement("select ID from processes where PROCESS = ?");
			pstmt.setString(1, processName);

			rs = pstmt.executeQuery();
			
			while(rs.next()) {
			ProcessnameAtlas processNameAtlas = new ProcessnameAtlas();
			
			if (rs.getString("ID") == null) {

			} else {
				processNameAtlas.setProcessId(Integer.parseInt(rs.getString("ID")));
			}
			list.add(k, processNameAtlas);
			k=k+1;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(mssqlconnection != null) {
					mssqlconnection.close();
				}
				if(rs!=null) {
					rs.close();
				}
				if(pstmt!=null) {
					pstmt.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	System.out.println("Process name list="+list);
		return list;
	}


}
