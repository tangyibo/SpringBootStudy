package com.weishao.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import com.weishao.migration.engine.MigrationMainEngine;

@SpringBootApplication
@Configuration("DataSourceConfig")
@ConfigurationProperties(prefix = "extract")
public class DataMigrationApplication {

	private static final Logger logger = LoggerFactory.getLogger(DataMigrationApplication.class);
	
	/*************************************************************
	spring:
		  datasource:
		    driver-class-name: com.mysql.jdbc.Driver
		    url: jdbc:mysql://172.16.90.210:3306/tangyb?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&useSSL=true
		    username: tangyibo
		    password: tangyibo
		      
	extract:
		  datasource:
		    driver-class-name: oracle.jdbc.driver.OracleDriver
		    url: jdbc:oracle:thin:@172.16.90.158:1521:ORCL
		    username: tangyibo
		    password: tangyibo
		  prefix: local_src_
		  schema: 
		      ODI:
		        - SNP_ACTION
		      HQTEST:
		        - TABLE_HELLO   
	***************************************************************/

	@Value("${extract.datasource.url}")
	private String srcJdbcUrl;

	@Value("${extract.datasource.driver-class-name}")
	private String srcDriverClassName;
	
	@Value("${extract.datasource.username}")
	private String srcUsername;

	@Value("${extract.datasource.password}")
	private String srcPassword;

	///////////////////////////////////////////

	@Value("${spring.datasource.url}")
	private String destJdbcUrl;

	@Value("${spring.datasource.driver-class-name}")
	private String destDriverClassName;

	@Value("${spring.datasource.username}")
	private String destUsername;

	@Value("${spring.datasource.password}")
	private String destPassword;

	////////////////////////////////////////////
	
	@Value("${extract.prefix}")
	private String tablePrefix;
	
	@Value("${extract.fetch-size}")
	private int fetchSize;
	
	private Map<String, List<String>> schema = new HashMap<>();
	
	////////////////////////////////////////////
	
	public String getTablePrefix() {
		return this.tablePrefix;
	}
	
	public int getFetchSize() {
		return this.fetchSize;
	}
	
	public Map<String, List<String>> getSchema(){
		return this.schema;
	}

	public void setSchema(Map<String, List<String>> schema){
		this.schema=schema;
	}
	
    @Bean(name = "sourceDataSource")
    public DataSource sourceDataSource() {
		logger.info("SourceDataSource config:\n jdbc-url:{}\n dirver-class-name:{}\n username:{}\n password:{}",
				this.srcJdbcUrl,this.srcDriverClassName,this.srcUsername,this.srcPassword);
		
    	return DataSourceBuilder.create()
    			.driverClassName(this.srcDriverClassName)
    			.url(this.srcJdbcUrl)
    			.username(this.srcUsername)
    			.password(this.srcPassword)
    			.build();
    }

	@Bean(name = "sourceJdbcTemplate")
	public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	////////////////////////////////////////////

    @Bean(name = "targetDataSource")
    public DataSource targetDataSource() {
		logger.info("TargetDataSource config:\n jdbc-url:{}\n dirver-class-name:{}\n username:{}\n password:{}",
				this.destJdbcUrl,this.destDriverClassName,this.destUsername,this.destPassword);
    	
    	return DataSourceBuilder.create()
    			.driverClassName(this.destDriverClassName)
    			.url(this.destJdbcUrl)
    			.username(this.destUsername)
    			.password(this.destPassword)
    			.build();
    }

	@Bean(name = "targetJdbcTemplate")
	public JdbcTemplate targetJdbcTemplate(@Qualifier("targetDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	/*************************************************************
	*
	*  以上为读取配置文件，并初始化源端和目的端的数据源，并生成相应的
	*  一对JdbcTemplate，作为引擎初始化的参数
	*  
	**************************************************************/
	
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(DataMigrationApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		ApplicationContext context=springApplication.run(args);

		try {
			while(true) {
			
			JdbcTemplate sourceJdbcTemplate = (JdbcTemplate) context.getBean("sourceJdbcTemplate");
			JdbcTemplate targetJdbcTemplate = (JdbcTemplate) context.getBean("targetJdbcTemplate");
			MigrationMainEngine app = new MigrationMainEngine(sourceJdbcTemplate, targetJdbcTemplate);
			DataMigrationApplication config = (DataMigrationApplication) context.getBean("DataSourceConfig");
			String targetTablePrefix = config.getTablePrefix();
			
			Map<String, List<String>> nameSchemaTableLists = config.getSchema();
			Map<String, Map<String,String>> tableMappers=new HashMap<String, Map<String,String>>();
			for (Entry<String, List<String>> entry : nameSchemaTableLists.entrySet()) {
				String schemaName=entry.getKey();
				List<String> tableNames=entry.getValue();
				
				Map<String,String> item=new HashMap<String,String>();
				for(String name:tableNames) {
					item.put(name, targetTablePrefix+name);
				}
				
				if(item.size()>0) {
					tableMappers.put(schemaName, item);
				}
			}

			app.run(tableMappers,config.getFetchSize());
			logger.info("migration over!");
			}
		} catch (Exception e) {
			logger.error("migration error:", e);
		}
	}

}
