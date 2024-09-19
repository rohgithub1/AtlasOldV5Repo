package com.cci.rest.service.database.config;

/**
 * Created by Niranjan on 27-03-2020.
 */

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseService {

	public Connection getConnection();
	//public Connection getBPConnection() throws SQLException;
}