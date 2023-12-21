package com.hty.controller.user;

import com.hty.service.user.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author hty
 * @date 2023-12-21 9:52
 * @email 1156388927@qq.com
 * @description
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /***
     * 给用户充值余额(假接口) TODO:完成测试后需要删除该接口
     * @param userId
     * @param count 充值的token数量
     * @return
     */
    @PostMapping("/top/up")
    public String topUp(Integer userId,Integer count){
        return userService.topUp(userId,count);
    }


}
