package com.cci.rest.service.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cci.rest.service.database.config.DatabaseService;
import com.cci.rest.service.dataobjects.AtlasMinimumCommentLength;

@Component
public class AtlasMinimumCommentLengthDao {

	@Autowired
	private DatabaseService databaseSerive;

	public List<AtlasMinimumCommentLength> getCommentMinLength() throws SQLException {

		List<AtlasMinimumCommentLength> list = new ArrayList();

		Connection mssqlconnection = databaseSerive.getConnection();
		Statement stmt = null;
		try {
			int k = 0;

			stmt = mssqlconnection.createStatement();

			ResultSet rs = stmt.executeQuery(
					"select top 1 COMMENT_MIN_LENGTH from PROCESS_DATA_ACTIVITY where COMMENT_MIN_LENGTH is not null ");
			while (rs.next()) {
				AtlasMinimumCommentLength ComLen = new AtlasMinimumCommentLength();

				if (rs.getString("COMMENT_MIN_LENGTH").toString() == null) {

				} else {

					String commentLength = rs.getString("COMMENT_MIN_LENGTH");
					ComLen.setCommentLength(Integer.parseInt(commentLength));
				}

				list.add(k, ComLen);
				k = k + 1;

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mssqlconnection != null) {
				mssqlconnection.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}

		return list;

	}

	{

	}
}
