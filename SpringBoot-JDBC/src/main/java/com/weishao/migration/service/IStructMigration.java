package com.weishao.migration.service;

import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import com.weishao.migration.except.UnsupportDatabaseTypeException;
import com.weishao.migration.except.UnsupportJdbcTypeMapper;
import com.weishao.migration.model.TableColumnDesc;

/**
 * 表结构迁移接口定义
 * @author tang
 *
 */
public interface IStructMigration {

	public void setSourceDatabase(JdbcTemplate jt) throws UnsupportDatabaseTypeException, SQLException;

	public List<TableColumnDesc> querySourceTableColumnDesc(String schemaName, String tableName)
			throws UnsupportDatabaseTypeException;

	public List<String> querySourceTablePrimaryKeys(String schemaName, String tableName) throws SQLException;

	public String getMysqlCreateSentence(List<TableColumnDesc> fieldNames, List<String> primaryKeys, String tableName,
			boolean createIfNotExist) throws UnsupportJdbcTypeMapper;

}
