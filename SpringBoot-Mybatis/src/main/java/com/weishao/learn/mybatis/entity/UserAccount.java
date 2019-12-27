package com.weishao.learn.mybatis.entity;

import java.util.Date;
import lombok.Data;

@Data
public class UserAccount {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String enabled;
    private Date createTime;
    private Date updateTime;
}
