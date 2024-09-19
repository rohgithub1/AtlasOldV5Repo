package com.cci.rest.service.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
//import com.cci.rest.service.dataobjects.AtlasUsers;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.springframework.stereotype.Component;

import com.cci.rest.service.constants.Constants;
import com.cci.rest.service.dataobjects.AtlasUsers;
//import com.cci.rest.service.dataobjects.Users;

@Component
public class AtlasUsersDao {

	@Autowired
	private DatabaseService databaseSerive;
	
	public boolean UpdateLastLogin(AtlasUsers user) 
	{
		AtlasUtility utility = new AtlasUtility();
		
		 
   
		
		String lastLogin = utility.getFormatedDateTimeString(user.getLastLogin());
		System.out.println(lastLogin);
		String modifiedDate = utility.getFormatedDateTimeString(user.getModifiedDate());
		System.out.println(modifiedDate);
		boolean result = true;
		
		
		
		try 
		{

    		Connection mssqlconnection = databaseSerive.getConnection();
    		Statement stmt=mssqlconnection.createStatement(); 
    		
    		 stmt.execute(" UPDATE USERS SET MODIFIED_BY = '" + user.getModifiedBy().toString().trim() + "',MODIFIED_DATE = '"+ modifiedDate
       				 +"',LAST_LOGIN = '" + lastLogin + "' WHERE DOMAIN_LOGIN_NAME = '" + user.getDomainLoginName() + "' AND IS_ACTIVE = 1");
        				 		
			 		
    	
    				 		
				int count =  stmt.getUpdateCount();
		  		 
		  		 System.out.println(count);
			
		  		if (count == 1) {
					System.out.println("Last Login Updated Sucessfully");
					result = true;
				}
			 
			
		}
		catch(Exception e) 
		{
			result = false;
			e.printStackTrace();
			
		}
		return result;
	}
	
	
	/*
	 * Get users active list
	 */
	
	//private static CCSLogger logger = CCSLogger.getInstance(LoginAuthenticator.class);
			/**
			 * This method will authenticate user
			 * 
			 * @param userName
			 *            <code>String</code>
			 * @param password
			 *            <code>String</code>
			 * @return true <code>boolean</code> if user is authenticated
			 *         else false.
			 */
			public AtlasUsers authenticateUser(String userName, String password)
			{
				AtlasUsers user = new AtlasUsers();
				HashMap<String,String> userListMap = new HashMap<String,String>();
				Hashtable<Object, Object> srchEnv = new Hashtable<Object, Object>();
				srchEnv.put(Context.INITIAL_CONTEXT_FACTORY, Constants.INITIAL_CONTEXT_FACTORY.toString());
				srchEnv.put(Context.PROVIDER_URL, Constants.PROVIDER_URL.toString());
				srchEnv.put(Context.SECURITY_AUTHENTICATION, Constants.SECURITY_AUTHENTICATION.toString());
				srchEnv.put(Context.SECURITY_PRINCIPAL, userName+Constants.MAIL_ID_EXTENSION);
				srchEnv.put(Context.SECURITY_CREDENTIALS, password);
				// Enable connection pooling
				srchEnv.put("com.sun.jndi.ldap.connect.pool", Constants.POOLING_STATUS);
				srchEnv.put("com.sun.jndi.ldap.connect.pool.maxsize" , Constants.MAX_SIZE);
				srchEnv.put("com.sun.jndi.ldap.connect.pool.timeout" , Constants.TIMEOUT);
				srchEnv.put("com.sun.jndi.ldap.connect.pool.initsize" , Constants.INITIAL_SIZE);
				srchEnv.put("com.sun.jndi.ldap.connect.pool.prefsize" , Constants.PREFERED_SIZE );

				boolean isUserAuthentic = false;
				InitialLdapContext srchContext = null;
				
				try
				{
					srchContext = new InitialLdapContext(srchEnv, null);
					
					isUserAuthentic = true;
					
					SearchControls constraints = new SearchControls();
					constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
					//NOTE: The attributes mentioned in array below are the ones that will be retrieved, you can add more.
					String[] attrIDs = { "distinguishedName",
					"sAMAccountName",
					"sn",
					"cn",
					"givenname",
					"mail",
					"telephonenumber", "canonicalName","userAccountControl","accountExpires"};
					constraints.setReturningAttributes(attrIDs);
				
					//NOTE: replace DC=domain,DC=com below with your domain info. It is essentially the Base Node for Search.
					
					NamingEnumeration answer = srchContext.search("DC=ccrn,DC=com", "sAMAccountName="
					+ userName, constraints);
					
//					For getting active users list
					String searchFilter = "(&(objectCategory=person))";
					NamingEnumeration userList = srchContext.search("OU=Pune,OU=Sites,DC=ccrn,DC=com", searchFilter, constraints);
					
					  
					  
					while (userList.hasMore()) {
						
						Attributes attrs = ((SearchResult) userList.next()).getAttributes();
						
						String username = attrs.get("sAMAccountName").toString();
						String fullName = attrs.get("CN").toString();
						
						username = username.replace("sAMAccountName:","").trim();
						fullName = fullName.replace("cn:","").trim();
						
						userListMap.put(username,fullName);
						
						
						
//						System.out.println(username);
//						System.out.println(fullName);
						
					}
			
					
//					End of getting active users list
					
					if (answer.hasMore()) {
						Attributes attrs = ((SearchResult) answer.next()).getAttributes();
						
						String givenName = attrs.get("givenName").toString();
						String [] Firstname = givenName.split(":");
					
						
						String sn = attrs.get("sn").toString();
						String [] Lastname = sn.split(":");
						
						//user.setFullname(Firstname[1].toString()+" " + Lastname[1].toString());
					} else {
						throw new Exception("Invalid User");
					}
					
				}
				catch (AuthenticationException authEx)
				{
					isUserAuthentic = false;
					authEx.printStackTrace();
				}
				catch (NamingException namEx)
				{
					isUserAuthentic = false;
					namEx.printStackTrace();
				}
				catch (Exception e)
				{
					isUserAuthentic = false;
					e.printStackTrace();
				}
				finally
				{
					try 
					{
						if(srchContext != null)
						{
							srchContext.close();
						}	
					} 
					catch (NamingException e) 
					{
						//logger.fatal(e.getMessage());
						e.getMessage();
					}
				}
				user.setAutheticateUser(isUserAuthentic);
				user.setDomainLoginName(userName);
				user.setActiveUsersList(userListMap);
				
				return user;
				
			}

}
