package com.weishao.migration.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * JDBC操作数据库工具类
 * 
 * @author Tang
 *
 */
public class JdbcTemplateUtil {

	public static String getDatabaseProduceName(JdbcTemplate jdbcTemplate) throws SQLException {
		
		return jdbcTemplate.execute(new ConnectionCallback<String>() {

			@Override
			public String doInConnection(Connection con) throws SQLException, DataAccessException {
				DatabaseMetaData meta = con.getMetaData();
				
				String name = meta.getDatabaseProductName();
				if (name.toLowerCase().equals("microsoft sql server")) {
					name = "sqlserver";
				}

				return name;
			}
			
		});
		
	}

	public static List<Map<String, String>> selectAll(JdbcTemplate jdbcTemplate, String sql) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		jdbcTemplate.query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				ResultSetMetaData metaData = rs.getMetaData();
				Map<String, String> item = new HashMap<String, String>();
				for (int j = 1; j <= metaData.getColumnCount(); ++j) {
					String key = metaData.getColumnName(j);
					if (null == key) {
						key = metaData.getColumnLabel(j);
					}

					String value = rs.getString(key);
					item.put(key, value);
				}
				result.add(item);
			}
		});

		return result;
	}
}
