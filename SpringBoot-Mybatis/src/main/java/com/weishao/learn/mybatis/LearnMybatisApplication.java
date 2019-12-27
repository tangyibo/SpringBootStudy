package com.weishao.learn.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mybatis学习demo
 * 
 * ==============================
 * CREATE TABLE `dbsync_user_account` (
 *   `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
 *   `username` varchar(255) NOT NULL COMMENT '登录的用户名',
 *   `password` varchar(255) NOT NULL COMMENT '登录的密码（MD5值）',
 *   `nickname` varchar(255) NOT NULL COMMENT '显示的昵称',
 *   `phone` varchar(255) DEFAULT NULL COMMENT '移动电话',
 *   `email` varchar(255) DEFAULT NULL COMMENT '电子邮箱',
 *   `enabled` enum('1','0') NOT NULL DEFAULT '1' COMMENT '帐号是否可用',
 *   `create_time` datetime NOT NULL COMMENT '记录创建时间',
 *   `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
 *   PRIMARY KEY (`id`) USING BTREE
 * ) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
 * 
 * @author tang
 *
 */

@SpringBootApplication
@MapperScan("com.weishao.learn.mybatis.dao")
public class LearnMybatisApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnMybatisApplication.class, args);
	}

}
