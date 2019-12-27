package com.weishao.learn.mybatis.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.weishao.learn.mybatis.dao.UserRespositoryMapper;
import com.weishao.learn.mybatis.entity.UserAccount;


@Service
public class TestService {

	@Autowired(required=true)
	private UserRespositoryMapper userRespository;
	
	public List<UserAccount>  findAll() {
		return userRespository.findAll();
	}
	
	public UserAccount findUserById(Integer id) {
		return userRespository.findUserById(id);
	}
	
	public void modifyUserById(UserAccount user) {
		userRespository.modifyUserById(user);
	}
	
	public void addUser(UserAccount user) {
		userRespository.addUser(user);
	}
	
	public void deleteUser(Integer id) {
		userRespository.deleteUser(id);
	}
}
