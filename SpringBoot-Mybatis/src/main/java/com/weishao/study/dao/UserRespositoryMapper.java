package com.weishao.study.dao;

import java.util.List;
import com.weishao.study.pojo.UserAccount;

public interface UserRespositoryMapper {
	
	public List<UserAccount> selectAll();
	
}
