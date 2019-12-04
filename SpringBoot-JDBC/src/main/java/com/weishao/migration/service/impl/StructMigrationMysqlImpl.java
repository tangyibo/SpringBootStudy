package com.weishao.migration.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import com.weishao.migration.constant.DatabaseType;
import com.weishao.migration.except.UnsupportDatabaseTypeException;
import com.weishao.migration.except.UnsupportJdbcTypeMapper;
import com.weishao.migration.factory.JdbcTypeMapFactory;
import com.weishao.migration.factory.SqlTemplateFactory;
import com.weishao.migration.model.TableColumnDesc;
import com.weishao.migration.service.IStructMigration;
import com.weishao.migration.utils.JdbcTemplateUtil;

/**
 * 结构迁移至MySQL的实现类
 * @author tang
 *
 */
public class StructMigrationMysqlImpl implements IStructMigration{

	private JdbcTemplate jdbcTemplate;
	private DatabaseType typeDatabase;
	
	public StructMigrationMysqlImpl() {
		this.jdbcTemplate=null;
		this.typeDatabase=DatabaseType.UNKOWN;
	}
	
	@Override
	public void setSourceDatabase(JdbcTemplate jt) throws UnsupportDatabaseTypeException, SQLException {
		Assert.notNull(jt, "jdbcTemplate must not be null");
		String typeName=JdbcTemplateUtil.getDatabaseProduceName(jt).toLowerCase();
		DatabaseType sourceDbType=SqlTemplateFactory.getDatabaseTypeByName(typeName);
		if(DatabaseType.UNKOWN==sourceDbType) {
			throw new UnsupportDatabaseTypeException(String.format("Unsupport source database type:[%s]", typeName));
		}
		
		this.jdbcTemplate=jt;
		this.typeDatabase=sourceDbType;
	}
	
	@Override
	public List<TableColumnDesc> querySourceTableColumnDesc(String schemaName, String tableName)
			throws UnsupportDatabaseTypeException {
		return SqlTemplateFactory.querySourceTableColumnDesc(this.typeDatabase, this.jdbcTemplate, schemaName,
				tableName);
	}
	
	@Override
	public List<String> querySourceTablePrimaryKeys(String schemaName, String tableName) throws SQLException{
		return SqlTemplateFactory.querySourceTablePrimaryKeys(jdbcTemplate,schemaName,tableName);
	}
	
	@Override
	public String getMysqlCreateSentence(List<TableColumnDesc> fieldNames,List<String> primaryKeys,String tableName,boolean createIfNotExist) throws UnsupportJdbcTypeMapper {
		List<String> cols = new ArrayList<String>();
		
		for (TableColumnDesc column : fieldNames) {
			if (primaryKeys.contains(column.getLabalName())) {
				cols.add(String.format("`%s` %s NOT NULL", column.getLabalName(),
						JdbcTypeMapFactory.getMySqlFieldTypeFromJdbcType(column)));
			} else {
				if(column.getFieldTypeName().toUpperCase().equals("TIMESTAMP") || column.getFieldTypeName().toUpperCase().equals("DATETIME")) {
					cols.add(String.format("`%s` %s DEFAULT NULL", column.getLabalName(),"VARCHAR(64)"));
				}else {
					cols.add(String.format("`%s` %s DEFAULT NULL", column.getLabalName(),
							JdbcTypeMapFactory.getMySqlFieldTypeFromJdbcType(column)));
				}
			}
		}
		
		if(!primaryKeys.isEmpty()) {
			cols.add(String.format("PRIMARY KEY (%s)", String.join(",", primaryKeys)));
		}

		String ifNotExists="";
		if (createIfNotExist) {
			ifNotExists="IF NOT EXISTS";
		}

		String fieldColumns=String.join(",\n", cols);
		return String.format("CREATE TABLE %s `%s` (\n%s\n) ENGINE=InnoDB DEFAULT CHARSET=utf8 ", ifNotExists, tableName,fieldColumns);
	}

}
