package com.cci.rest.service.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.ActiveProcessList;
import com.cci.rest.service.dataobjects.UserProcessDetailsAtlas;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;

@Component
public class Atlas_ProcessesAssignedToUser {

	@Autowired
	private DatabaseService databaseService;


	public List<UserProcessDetailsAtlas> processesAssignedToUser(String username) throws SQLException {

		System.out.println(username);
		List<UserProcessDetailsAtlas> list = new ArrayList<>();
		Connection mssqlconnection = null;
		PreparedStatement pstmt= null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();
			pstmt = mssqlconnection.prepareStatement("select Mapping_data from user_process_Mapping where USERNAME = ? and is_active=1");
			pstmt.setString(1, username);
			int k = 0;
			rs = pstmt.executeQuery();
			while (rs.next()) {

				UserProcessDetailsAtlas processDataAtlas = new UserProcessDetailsAtlas();

				if (rs.getString("MAPPING_DATA") == null) {
				} else {
					processDataAtlas.setProcessList(rs.getString("MAPPING_DATA"));
				}

				list.add(k, processDataAtlas);
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
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return list;

	}

	public LinkedHashMap<String, String> getColumnNameForProcess(int processId) throws SQLException {

		LinkedHashMap<String, String> keys = new LinkedHashMap<>();
		String key;
		String values = "";
		String colName = "";
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		
		try {
			mssqlconnection = databaseService.getConnection();
			if (processId > 0) {
				String res = "";
				String processColumnQuery = "select Process_Columns from processes where ID = ? and Process_Columns is not null and IS_ACTIVE = 1 order by MODIFIED_DATE desc";
				pstmt = mssqlconnection.prepareStatement(processColumnQuery);
				pstmt.setInt(1, processId);
				rs1 = pstmt.executeQuery();
				
				if (rs1.next()) {
					res = rs1.getString(1);

					res = res.replace("{{", "{");

					JSONObject newUserJSON = new JSONObject(res);

					for (Iterator<String> it = newUserJSON.keys(); it.hasNext();) {
						key = it.next();

						if (key == "processId" || key == "startTime" || key == "endTime" || key == "duration"
								|| key == "isActive" || key == "createdBy" || key == "createdDate"
								|| key == "modifiedBy" || key == "modifiedDate" || key == "PROCESS"
								|| key == "DESCRIPTION" || key == "IS_ACTIVE" || key == "CREATED_DATE"
								|| key == "MODIFIED_BY" || key == "MODIFIED_DATE") {

						} else {
							colName = key;
							values = newUserJSON.getString(key);
						}
						keys.put(colName.trim(), values.trim());
					}

					keys.remove("processId");
					keys.remove("startTime");
					keys.remove("endTime");
					keys.remove("duration");
					keys.remove("isActive");
					keys.remove("createdBy");
					keys.remove("createdDate");
					keys.remove("modifiedBy");
					keys.remove("modifiedDate");
					keys.remove("PROCESS");
					keys.remove("DESCRIPTION");
					keys.remove("IS_ACTIVE");
					keys.remove("CREATED_DATE");
					keys.remove("MODIFIED_BY");
					keys.remove("MODIFIED_DATE");
					keys.remove("CREATED_BY");

				}

				else {
					pstmt=mssqlconnection.prepareStatement("select process_column_Data from revampAtlas_process_data where PROCESS_ID = ?");
					pstmt.setInt(1, processId);
					rs = pstmt.executeQuery();

					if (rs.next()) {
						res = rs.getString(1);

						JSONObject newUserJSON = new JSONObject(res);

						for (Iterator<String> it = newUserJSON.keys(); it.hasNext();) {
							key = it.next();
							values = newUserJSON.getString(key);
							keys.put(key.trim(), values.trim());
							;
						}

						keys.remove("processId");
						keys.remove("startTime");
						keys.remove("endTime");
						keys.remove("duration");
						keys.remove("isActive");
						keys.remove("createdBy");
						keys.remove("createdDate");
						keys.remove("modifiedBy");
						keys.remove("modifiedDate");
						keys.remove("PROCESS");
						keys.remove("DESCRIPTION");
						keys.remove("IS_ACTIVE");
						keys.remove("CREATED_DATE");
						keys.remove("MODIFIED_BY");
						keys.remove("MODIFIED_DATE");
						keys.remove("CREATED_BY");
					}
				}

			} else

			{
				System.out.println("No process selected");
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
				}if(rs1!=null) {
					rs1.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		return keys;
	}
	
	//Return all active process List 
	public List<ActiveProcessList> getAllProcessName() {
		//get all active activities 
		List<ActiveProcessList> list = new ArrayList<>();
		Connection mssqlconnection = null;
		PreparedStatement pstmt= null;
		ResultSet rs = null;
		try {
			mssqlconnection = databaseService.getConnection();
			pstmt = mssqlconnection.prepareStatement("select ID,PROCESS,DESCRIPTION,Process_Columns from Processes where Is_active =1;");
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				ActiveProcessList process=new ActiveProcessList();
				process.setProcessId(Integer.parseInt(rs.getString("ID")));
				process.setProcessName(rs.getString("PROCESS"));
				process.setDescription(rs.getString("DESCRIPTION"));
				process.setProcess_Columns(rs.getString("Process_Columns"));
				list.add(process);
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
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return list;
		
	}

}
