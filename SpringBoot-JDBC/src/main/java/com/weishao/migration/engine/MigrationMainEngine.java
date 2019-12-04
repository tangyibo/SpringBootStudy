package com.weishao.migration.engine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import com.weishao.migration.model.TableColumnDesc;
import com.weishao.migration.service.IDataMigration;
import com.weishao.migration.service.IStructMigration;
import com.weishao.migration.service.impl.DataMigrationMysqlImpl;
import com.weishao.migration.service.impl.StructMigrationMysqlImpl;

public class MigrationMainEngine {

	private static final Logger logger = LoggerFactory.getLogger(MigrationMainEngine.class);
	
	private JdbcTemplate sourceJdbcTemplate;
	
	private JdbcTemplate targetJdbcTemplate;

	public MigrationMainEngine(JdbcTemplate sourceJdbcTemplate,JdbcTemplate targetJdbcTemplate) {
		Assert.notNull(sourceJdbcTemplate, "source jdbcTemplate must not be null");
		Assert.notNull(targetJdbcTemplate, "target jdbcTemplate must not be null");
		this.sourceJdbcTemplate=sourceJdbcTemplate;
		this.targetJdbcTemplate=targetJdbcTemplate;
	}

	public void run(Map<String, Map<String,String>> tableMappers,int fetchSize) throws Exception {

		IStructMigration sm = new StructMigrationMysqlImpl();
		sm.setSourceDatabase(sourceJdbcTemplate);

		IDataMigration dm = new DataMigrationMysqlImpl(fetchSize);
		dm.setSourceDatabase(sourceJdbcTemplate);
		dm.setTargetDatabase(targetJdbcTemplate);

		logger.info("run migration for mapper:{}", tableMappers.toString());
		for (Entry<String, Map<String,String>> entry : tableMappers.entrySet()) {
			String sourceSchemaName = entry.getKey();
			Map<String,String> mapperTableNames = entry.getValue();

			for (Entry<String,String> item : mapperTableNames.entrySet()) {
				String sourceTableName=item.getKey();
				String targetTableName=item.getValue();
				
				// 处理表结构
				List<TableColumnDesc> fieldNames = sm.querySourceTableColumnDesc(sourceSchemaName, sourceTableName);
				List<String> primaryKeys = sm.querySourceTablePrimaryKeys(sourceSchemaName, sourceTableName);
				String createTableSql = sm.getMysqlCreateSentence(fieldNames, primaryKeys, targetTableName, true);
				targetJdbcTemplate.execute(createTableSql);
				logger.info("create table {} success.",targetTableName);

				// 处理表数据
				targetJdbcTemplate.execute(String.format("truncate table `%s`", targetTableName));
				logger.info("truncate table {} success.",targetTableName);

				dm.dataTransfer(sourceSchemaName, sourceTableName, targetTableName, fieldNames);
			}
		}
	}
}
