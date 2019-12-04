package com.weishao.migration.factory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import com.weishao.migration.constant.DatabaseType;
import com.weishao.migration.except.UnsupportDatabaseTypeException;
import com.weishao.migration.model.TableColumnDesc;

public class SqlTemplateFactory {

	private static Map<String, String> sqlColumnFieldsTemplates = new HashMap<String, String>();

	static {
		// 获取列字段描述相关SQL模版
		sqlColumnFieldsTemplates.put(DatabaseType.MYSQL.name().toLowerCase(), "select * from  `%s`.`%s` limit 1 ");
		sqlColumnFieldsTemplates.put(DatabaseType.ORACLE.name().toLowerCase(), "select * from \"%s\".\"%s\" where rownum<1 ");
		sqlColumnFieldsTemplates.put(DatabaseType.SQLSERVER.name().toLowerCase(), "select top 1 * from [%s].[%s] ");
	}
	
	public static DatabaseType getDatabaseTypeByName(String dbtype) {
		if(SqlTemplateFactory.sqlColumnFieldsTemplates.containsKey(dbtype.toLowerCase())) {
			if(dbtype.toLowerCase().equals(DatabaseType.MYSQL.name().toLowerCase())) {
				return DatabaseType.MYSQL;
			}else if (dbtype.toLowerCase().equals(DatabaseType.ORACLE.name().toLowerCase())) {
				return DatabaseType.ORACLE;
			}else if (dbtype.toLowerCase().equals(DatabaseType.SQLSERVER.name().toLowerCase())) {
				return DatabaseType.SQLSERVER;
			}
		}
		
		return DatabaseType.UNKOWN;
	}

	public static List<TableColumnDesc> querySourceTableColumnDesc(DatabaseType sourceDbType,JdbcTemplate jdbcTemplate, String schemaName, String tableName) 
			throws UnsupportDatabaseTypeException {
		String sql = SqlTemplateFactory.getColumnFieldsTemplateSql(sourceDbType, schemaName, tableName);
		List<TableColumnDesc> results = new ArrayList<TableColumnDesc>();

		// 当表或视图中有数据时
		jdbcTemplate.query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				ResultSetMetaData m = rs.getMetaData();
				for (int i = 1; i <= m.getColumnCount(); ++i) {
					TableColumnDesc columnDesc = new TableColumnDesc();
					columnDesc.setFieldName(m.getColumnName(i));
					columnDesc.setLabalName(m.getColumnName(i));
					columnDesc.setFieldType(m.getColumnType(i));
					if(0!=columnDesc.getFieldType()) {
						columnDesc.setFieldTypeName(m.getColumnTypeName(i));
						columnDesc.setFiledTypeClassName(m.getColumnClassName(i));
						columnDesc.setDisplaySize(m.getColumnDisplaySize(i));
						columnDesc.setPrecisionSize(m.getPrecision(i));
						columnDesc.setScaleSize(m.getScale(i));
						columnDesc.setAutoIncrement(m.isAutoIncrement(i));
						columnDesc.setNullable(m.isNullable(i) == 1 ? true : false);
					}else {
						//处理视图中NULL as fieldName的情况
						columnDesc.setFieldTypeName("VARCHAR");
						columnDesc.setFiledTypeClassName(String.class.getName());
						columnDesc.setDisplaySize(255);
						columnDesc.setPrecisionSize(255);
						columnDesc.setScaleSize(0);
						columnDesc.setAutoIncrement(false);
						columnDesc.setNullable(true);
					}
					
					results.add(columnDesc);
				}
			}

		});

		if (results.isEmpty()) {
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
			SqlRowSetMetaData m = rowSet.getMetaData();
			int columnCount = m.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				TableColumnDesc columnDesc = new TableColumnDesc();
				columnDesc.setFieldName(m.getColumnName(i));
				columnDesc.setLabalName(m.getColumnName(i));
				columnDesc.setFieldType(m.getColumnType(i));
				columnDesc.setFieldTypeName(m.getColumnTypeName(i));
				columnDesc.setFiledTypeClassName(m.getColumnClassName(i));
				columnDesc.setDisplaySize(m.getColumnDisplaySize(i));
				columnDesc.setPrecisionSize(m.getPrecision(i));
				columnDesc.setScaleSize(m.getScale(i));
				columnDesc.setAutoIncrement(false);
				columnDesc.setNullable(true);
				results.add(columnDesc);
			}
		}

		return results;
	}

	public static List<String> querySourceTablePrimaryKeys(JdbcTemplate jdbcTemplate,String schemaName, String tableName)
			throws SQLException {		
		return jdbcTemplate.execute(new ConnectionCallback<List<String>>() {

			@Override
			public List<String> doInConnection(Connection con) throws SQLException, DataAccessException {
				DatabaseMetaData meta = con.getMetaData();
				List<String> results = new ArrayList<String>();
				ResultSet rs = meta.getPrimaryKeys(null, schemaName, tableName);
				
				while (rs.next()) {
					results.add(rs.getString("COLUMN_NAME"));
				}

				return results;
			}
			
		});
	}
	
	private static String getColumnFieldsTemplateSql(DatabaseType sourceDbType, String schemaName, String tableName)
			throws UnsupportDatabaseTypeException {
		if (sqlColumnFieldsTemplates.containsKey(sourceDbType.name().toLowerCase())) {
			return String.format(sqlColumnFieldsTemplates.get(sourceDbType.name().toLowerCase()), schemaName,tableName);
		}

		throw new UnsupportDatabaseTypeException(String.format("Unsupport database type:[%s]", sourceDbType.name()));
	}
}
