package com.cci.rest.service.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cci.rest.service.database.config.DatabaseService;


@Component
public class AtlasMappingDataDao {

	@Autowired
	private DatabaseService databaseSerive;
	
	public HashMap<String,String> getMappingProperties() 
	{
		HashMap<String,String> mappinglist = new HashMap<String,String>();
		Connection mssqlconnection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{
			mssqlconnection = databaseSerive.getConnection();
			pstmt = mssqlconnection.prepareStatement("select * from mapping_data md where md.is_active = 1");
			rs=pstmt.executeQuery();
    		 
    		while(rs.next())
    		{    			
    			if(rs.getString("map_from") == null)
    			{}
    			else
    			{
    				mappinglist.put(rs.getString("map_from"), rs.getString("map_to"));
    			}
    		}
		} 
		catch (Exception e) 
		{
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
		return mappinglist;
	}
}
