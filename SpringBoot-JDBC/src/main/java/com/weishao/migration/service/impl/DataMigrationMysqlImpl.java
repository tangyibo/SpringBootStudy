package com.weishao.migration.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import com.weishao.migration.constant.DatabaseType;
import com.weishao.migration.except.UnsupportDatabaseTypeException;
import com.weishao.migration.factory.SqlTemplateFactory;
import com.weishao.migration.model.TableColumnDesc;
import com.weishao.migration.service.IDataMigration;
import com.weishao.migration.utils.JdbcTemplateUtil;

/**
 * 数据迁移至MySQL的实现类
 * @author tang
 *
 */
public class DataMigrationMysqlImpl implements IDataMigration {
	
	private static final Logger logger = LoggerFactory.getLogger(DataMigrationMysqlImpl.class);
	private static Integer DATA_FETCH_SIZE=10000;//设置FetchSize的大小
	private static Integer QUERY_TIMEOUT_SECONDS=5*60;//查询的超时时间

	private JdbcTemplate sourceJdbcTemplate;
	private JdbcTemplate targetJdbcTemplate;
	private int fetchSize;
	
	public DataMigrationMysqlImpl(int fetchSize) {
		this.sourceJdbcTemplate=null;
		this.targetJdbcTemplate=null;
		if(fetchSize>0) {
			this.fetchSize=fetchSize;
		}else {
			this.fetchSize=DATA_FETCH_SIZE;
		}
	}
	
	@Override
	public void setSourceDatabase(JdbcTemplate jt) throws UnsupportDatabaseTypeException, SQLException{
		Assert.notNull(jt, "jdbcTemplate must not be null");
		String typeName=JdbcTemplateUtil.getDatabaseProduceName(jt).toLowerCase();
		DatabaseType sourceDbType=SqlTemplateFactory.getDatabaseTypeByName(typeName);
		if(DatabaseType.UNKOWN==sourceDbType) {
			throw new UnsupportDatabaseTypeException(String.format("Unsupport source database type:[%s]", typeName));
		}
		
		this.sourceJdbcTemplate=jt;
	}
	
	@Override
	public void setTargetDatabase(JdbcTemplate jt) throws UnsupportDatabaseTypeException, SQLException{
		Assert.notNull(jt, "jdbcTemplate must not be null");
		String typeName=JdbcTemplateUtil.getDatabaseProduceName(jt).toLowerCase();
		DatabaseType sourceDbType=SqlTemplateFactory.getDatabaseTypeByName(typeName);
		if(DatabaseType.MYSQL!=sourceDbType) {
			throw new UnsupportDatabaseTypeException(String.format("Unsupport target database type:[%s]", typeName));
		}
		
		this.targetJdbcTemplate=jt;
	}
	
	@Override
	public void dataTransfer(String sourceSchemaName,String sourceTableName, String targetTableName, List<TableColumnDesc> fieldNames) throws SQLException, UnsupportDatabaseTypeException {
		String typeName=JdbcTemplateUtil.getDatabaseProduceName(this.sourceJdbcTemplate).toLowerCase();
		DatabaseType sourceDbType=SqlTemplateFactory.getDatabaseTypeByName(typeName);
		if(DatabaseType.UNKOWN==sourceDbType) {
			throw new UnsupportDatabaseTypeException(String.format("Unsupport target database type:[%s]", typeName));
		}
		
		String quotation=this.getQuotationChar(sourceDbType);
		String sourceTableFullName=this.getDatabaseTableFullName(sourceDbType, sourceSchemaName, sourceTableName);
		
		StringBuilder keysSource=new StringBuilder();
		StringBuilder keysTarget=new StringBuilder();
		StringBuilder vals=new StringBuilder();
		for(int i=0;i<fieldNames.size();++i) {
			TableColumnDesc column=fieldNames.get(i);
			keysSource.append(quotation);
			keysSource.append(column.getFieldName());
			keysSource.append(quotation);
			
			keysTarget.append("`");
			keysTarget.append(column.getFieldName());
			keysTarget.append("`");
			
			vals.append("?");
			if(i!=fieldNames.size()-1) {
				keysSource.append(",");
				keysTarget.append(",");
				vals.append(",");
			}
		}

		String sqlQuery=String.format("select %s from %s",keysSource.toString(),sourceTableFullName);
		logger.info("query from source database sql:" + sqlQuery);
		String sqlInsert=String.format("insert into `%s`(%s) values(%s)",targetTableName,keysTarget.toString(),vals.toString());
		logger.info("insert to target mysql database sql:" + sqlInsert);
		
		long startTime = System.currentTimeMillis();
		this.doDataMigration(sourceDbType,sqlQuery, sqlInsert);
		long endTime = System.currentTimeMillis();
		float seconds = (endTime - startTime) / 1000F;
		logger.info("data transfer[ {} => {} ] ok,elipse seconds: {} ",sourceTableName,targetTableName,Float.toString(seconds));
	}

	private void doDataMigration(DatabaseType sourceDbType, String sqlQuery, String sqlInsert) throws SQLException {

		sourceJdbcTemplate.execute(new ConnectionCallback<Boolean>() {

			public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
				Statement stmt = null;
				ResultSet rs = null;
				long totalCount = 0;
				List<Object[]> records = new LinkedList<Object[]>();

				try {
					con.setAutoCommit(false);
					stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);// 设置查询的超时时间
					if (sourceDbType == DatabaseType.MYSQL) {
						// 由于MySQL不支持FetchSize功能，需要单独处理
						stmt.setFetchSize(Integer.MIN_VALUE);
					} else {
						// 对于其他数据库通过使用FetchSize功能来分批处理
						stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
						stmt.setFetchSize(fetchSize);
					}

					rs = stmt.executeQuery(sqlQuery);
					while (rs.next()) {
						ResultSetMetaData metaData = rs.getMetaData();
						Object args[] = new Object[metaData.getColumnCount()];
						int k = 0;
						for (int j = 1; j <= metaData.getColumnCount(); ++j) {
							try {
								args[k] = rs.getObject(j);
							} catch (SQLException e) {
								args[k] = null;
								// logger.warn("Process row when getObject() error:",e.getMessage());
							}

							++k;
						}

						totalCount++;
						records.add(args);
						if (records.size() >= fetchSize) {
							processRows(records, sqlInsert);
							logger.info("handle data total count:" + totalCount);
						}

					} // while

					JdbcUtils.closeResultSet(rs);
					rs = null;

					if (records.size() > 0) {
						processRows(records, sqlInsert);
						records.clear();
					}

					logger.info("handle table total item count:" + totalCount);
				} catch (SQLException ex) {
					// Release Connection early, to avoid potential connection pool deadlock
					// in the case when the exception translator hasn't been initialized yet.
					JdbcUtils.closeResultSet(rs);
					rs = null;
					JdbcUtils.closeStatement(stmt);
					stmt = null;
					throw ex;
				} finally {
					JdbcUtils.closeResultSet(rs);
					JdbcUtils.closeStatement(stmt);
				}

				return true;
			}
		});

	}
	
	private void processRows(List<Object[]> records, String sqlInsert) throws SQLException {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(targetJdbcTemplate.getDataSource());
		TransactionStatus status = transactionManager.getTransaction(def);
		
		try {
			for (Object[] args : records) {
				targetJdbcTemplate.update(sqlInsert, args);
			}
			
			records.clear();
			transactionManager.commit(status);
		} catch (TransactionException e) {
			transactionManager.rollback(status);
			throw e;
		}
		
	}
	
	private String getQuotationChar(DatabaseType dbtype) throws UnsupportDatabaseTypeException {
		if(dbtype==DatabaseType.MYSQL) {
			return "`";
		}else if(dbtype==DatabaseType.ORACLE) {
			return "\"";
		}else if(dbtype==DatabaseType.SQLSERVER) {
			return "\"";
		}else {
			throw new UnsupportDatabaseTypeException(String.format("Unsupport database type:[%s]", dbtype.name()));
		}
	}
	
	private String getDatabaseTableFullName(DatabaseType dbtype,String schemaName,String tableName) throws UnsupportDatabaseTypeException {
		if(dbtype==DatabaseType.MYSQL) {
			return String.format("`%s`.`%s`",schemaName,tableName);
		}else if(dbtype==DatabaseType.ORACLE) {
			return String.format("\"%s\".\"%s\"",schemaName,tableName);
		}else if(dbtype==DatabaseType.SQLSERVER) {
			return String.format("[%s].[%s]",schemaName,tableName);
		}else {
			throw new UnsupportDatabaseTypeException(String.format("Unsupport database type:[%s]", dbtype.name()));
		}
	}
}
