package com.cci.rest.service.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.SendEmail;


@Component
public class SendEmailDao {
	
	String createdBy;
	
	@Autowired
	private DatabaseService databaseSerive;

	public List<SendEmail> getManagerDetails(String userId){
		
		List<SendEmail> list = new ArrayList();
	
		try 
    	{
			
			SendEmail managerDetail = new SendEmail();
	
			
    		Connection mssqlconnection = databaseSerive.getConnection();
    		Statement stmt=mssqlconnection.createStatement(); 
    		
    		
    		if(!userId.isEmpty()) {
    			
    			
    			ResultSet rs=stmt.executeQuery("select * from users where DOMAIN_LOGIN_NAME = '"+userId.trim()+"'");
    			
    			while(rs.next())
        		{
    				SendEmail managerDetails = new SendEmail();
    				
    			
    				if(userId.length() == 0 || userId != null) {
    	    			
    		    		ResultSet rs1=stmt.executeQuery("Select CREATED_BY from users where DOMAIN_LOGIN_NAME ='"+userId.trim()+"' and IS_ACTIVE= 1");
    		    			
    		        		
    		        		while(rs1.next())
    		        		{
    		        			
    		        			
    		        			        			
    		        			if(rs1.getString("CREATED_BY") == null) {
    		        				
    		        			}
    		        			else {
    		        				managerDetails.setCreatedBy(rs1.getString("CREATED_BY").toString());
    		        				createdBy =managerDetails.getCreatedBy();
    		        			}
    		        			
    		        			
    		        		}
    		        		ResultSet rs2=stmt.executeQuery("select * from users where DOMAIN_LOGIN_NAME='"+managerDetails.getCreatedBy()+"' and IS_ACTIVE=1");
		            		
		            		while(rs2.next())
		            		{
		            			
		            			
		            			        			
		            			if(rs2.getString("FIRST_NAME") == null && rs2.getString("LAST_NAME") == null) {
		            				
		            			}
		            			else {
		            				managerDetails.setManagerName(rs2.getString("FIRST_NAME").toString() + " " +rs2.getString("LAST_NAME").toString());
		            			System.out.println(managerDetails.getManagerName());
		            			managerDetails.setErrorMessage("Email has been sent to " + managerDetails.getManagerName() + "!");
		            		
		        		
		            			}
		            			
		            		
		            		}
    			
    				
    			
    			}
    				System.out.println(managerDetails.getManagerName());
    				if(managerDetails.getManagerName() == null ) {
    				managerDetails.setErrorMessage("Manager for entered username not found!");
    				}
    		
            		list.add(managerDetails);
        		}
    			
    			
    		}
    		
    		managerDetail.setErrorMessage("Username dosen't exists in Database!");
    		
    		list.add(managerDetail);
			
		}
			
    	
		catch(Exception e) {
			
		}
		
		return list;
		
	}
}
