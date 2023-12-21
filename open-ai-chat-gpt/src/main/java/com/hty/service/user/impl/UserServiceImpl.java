package com.hty.service.user.impl;

import com.hty.dao.user.UserMapper;
import com.hty.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author hty
 * @date 2023-12-21 9:54
 * @email 1156388927@qq.com
 * @description
 */

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public String topUp(Integer userId,Integer count) {
        Integer rows = userMapper.updateUserBillLast(userId, count);
        if(rows != 1){
            log.info("充值失败");
        }
        return "";
    }
}
