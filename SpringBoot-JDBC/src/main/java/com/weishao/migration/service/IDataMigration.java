package com.weishao.migration.service;

import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import com.weishao.migration.except.UnsupportDatabaseTypeException;
import com.weishao.migration.model.TableColumnDesc;

/**
 * 表数据迁移接口定义
 * @author tang
 *
 */
public interface IDataMigration {

	public void setSourceDatabase(JdbcTemplate jt) throws UnsupportDatabaseTypeException, SQLException;
	
	public void setTargetDatabase(JdbcTemplate jt) throws UnsupportDatabaseTypeException, SQLException;

	public void dataTransfer(String sourceSchemaName,String sourceTableName, String targetTableName, List<TableColumnDesc> fieldNames) throws UnsupportDatabaseTypeException, SQLException;
}
