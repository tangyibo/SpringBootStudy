package com.weishao.study;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.weishao.study.service.TestService;

/**
 * 使用Mybatis操作数据库测试类
 * 
 * @author tang
 *
 */
@SpringBootTest
class SpringBootApplicationTests {

	@Autowired(required=true)
	TestService testService;
	
	@Test
	void contextLoads() {
		System.out.println(testService.findAll());
	}

}
