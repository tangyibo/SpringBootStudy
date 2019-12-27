package com.weishao.learn.mybatis;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.weishao.learn.mybatis.entity.UserAccount;
import com.weishao.learn.mybatis.service.TestService;

@SpringBootTest
class LearnMybatisApplicationTests {

	@Autowired(required=true)
	TestService testService;
	
	@Test
	void findAll() {
		System.out.println(testService.findAll());
	}

	@Test
	void test_select_use_ibatis() {
		System.out.println(testService.findUserById(1));
	}
	
	@Test
	void test_add_user_ibatis() {
		UserAccount user=new UserAccount();
		user.setNickname("xxxxxxx");
		user.setUsername("admin");
		user.setNickname("ssssssssss");
		user.setPassword("aaaaaaaaaa");
		user.setPhone("1222222222");
		user.setCreateTime(new Date());
		testService.addUser(user);
	}
	
	@Test
	void test_update_ibatis() {
		UserAccount user=new UserAccount();
		user.setId(3L);
		user.setNickname("xxxxxxx");
		user.setUsername("admin");
		testService.modifyUserById(user);
	}
	
	
	@Test
	void test_delete_ibatis() {
		testService.deleteUser(3);
	}
	

}
