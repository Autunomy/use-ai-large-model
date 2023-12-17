package com.hty.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hty
 * @date 2023-12-11 11:48
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String email;
    private String phoneNum;
    private String password;
    private Integer billLast;
    private Date createTime;
}
