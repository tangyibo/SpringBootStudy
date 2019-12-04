package com.weishao.study.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.weishao.study.pojo.UserAccount;

import com.weishao.study.dao.UserRespositoryMapper;

@Service
public class TestService {

	@Autowired(required=true)
	private UserRespositoryMapper userRespository;
	
	public List<UserAccount>  findAll() {
		return userRespository.selectAll();
	}
}
