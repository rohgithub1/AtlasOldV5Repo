package com.cci.rest.service.database.config;



/**
* Created by Niranjan on 27-03-2020.
* Modified by Rohit on 10/12/2022.
*/



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



@Service
public class DatabaseServiceImpl implements DatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServiceImpl.class);
   
    @Value("${atlas_user.datasource.username}")
    String usernameMssql;
    
    @Value("${atlas_user.datasource.password}")
    String passwordMssql;
    
    @Value("${atlas_user.datasource.url}")
    String connectionStringMssql;
    
    @Value("${datasource.driverClassName}")
    String driverClassName;



//    @Value("${rsa_user.datasource.tomcat.username}")
//    String usernameBP;
//    
//    @Value("${rsa_user.datasource.tomcat.password}")
//    String passwordBP;
//    
//    @Value("${rsa_user.datasource.tomcat.url}")
//    String connectionStringBP;
//    
//    @Value("${rsa_user.datasource.dbcp2.default-read-only}")
//    boolean readOnlyBP;
      



   @Override
    public Connection getConnection() {
         Connection dbcon = null;
        try {
            Class.forName(this.driverClassName);
            dbcon = DriverManager.getConnection(this.connectionStringMssql, this.usernameMssql, this.passwordMssql);
        } catch (ClassNotFoundException cnfe) {
            LOGGER.error("ERROR : DBConnect : Couldn't find the driver! {}",cnfe.getStackTrace());
        } catch (Exception se) {
            LOGGER.error("Couldn't connect: print out a stack trace and exit.{} ",se.getStackTrace());
        }
        return dbcon;
    }



//    public Connection getBPConnection() throws SQLException{
//        Connection dbcon=null;
//        try {
//            Class.forName(this.driverClassName);
//            dbcon = DriverManager.getConnection(this.connectionStringBP, this.usernameBP, this.passwordBP);
//        } catch (Exception se) {
//            LOGGER.error("Couldn't connect: print out a stack trace and exit. {}",se.getStackTrace());
//            throw new SQLException(se.getMessage());
//        }
//        return dbcon;
//    }
}