package com.weishao.learn.mybatis.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.type.JdbcType;

import com.weishao.learn.mybatis.entity.UserAccount;

public interface UserRespositoryMapper {
	
    @Select("select * from `dbsync_user_account` ")
    @Results(
            id = "id",
            value = {
                    @Result(column = "username", property = "username", javaType = String.class, jdbcType = JdbcType.VARCHAR),
                    @Result(column = "password", property = "password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
                    @Result(column = "nickname", property = "nickname", javaType = String.class, jdbcType = JdbcType.VARCHAR),
                    @Result(column = "phone", property = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
                    @Result(column = "email", property = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
                    @Result(column = "enabled", property = "enabled", javaType = String.class, jdbcType = JdbcType.INTEGER),
                    @Result(column = "create_time", property = "createTime", javaType = Date.class, jdbcType = JdbcType.DATE),
                    @Result(column = "update_time", property = "updateTime", javaType = Date.class, jdbcType = JdbcType.DATE)
            }
    )
	public List<UserAccount> findAll();
	
	@Select("SELECT * FROM `dbsync_user_account` where id=#{id} ")
	public UserAccount findUserById(@Param("id") Integer uid);

	@Update("UPDATE `dbsync_user_account` set username=#{username} , nickname=#{nickname} where id=#{id}")
	public void modifyUserById(UserAccount user);

	@Insert("INSERT INTO `dbsync_user_account` (`username`, `password`, `nickname`, `phone`, `email`, `enabled`,`create_time`)  "
			+ "VALUES (#{username}, #{password}, #{nickname}, #{phone}, #{email},'1',#{createTime})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	public void addUser(UserAccount user);

	@Delete("DELETE FROM `dbsync_user_account`  where id=#{id}")
	public void deleteUser(@Param("id") Integer uid);
}
