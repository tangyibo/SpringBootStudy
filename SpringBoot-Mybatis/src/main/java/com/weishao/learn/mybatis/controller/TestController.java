package com.weishao.learn.mybatis.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.weishao.learn.mybatis.entity.UserAccount;
import com.weishao.learn.mybatis.service.TestService;

@RestController
public class TestController {

	@Autowired
	TestService testService;

	@GetMapping("/findAll")
	public List<UserAccount> findAll() {
		return testService.findAll();
	}

	@GetMapping("/findOne/{id}")
	public UserAccount findOne(@PathVariable("id") Integer id) {
		return testService.findUserById(id);
	}

}
