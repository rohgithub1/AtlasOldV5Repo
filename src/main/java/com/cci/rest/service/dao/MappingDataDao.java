package com.cci.rest.service.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
//import com.cci.rest.service.dataobjects.MappingData;
//import com.cci.rest.service.dataobjects.ResourceStatus;

@Component
public class MappingDataDao {

	@Autowired
	private DatabaseService databaseSerive;
	
	public HashMap<String,String> getMappingProperties() 
	{
		HashMap<String,String> mappinglist = new HashMap<String,String>();
		try 
		{
			Connection mssqlconnection = databaseSerive.getConnection();
			
			Statement stmt=mssqlconnection.createStatement();  
    		ResultSet rs=stmt.executeQuery("select * from mapping_data md where md.is_active = 1"); 
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
		}
		return mappinglist;
	}
}
