package com.hty.service.user;

/**
 * @author hty
 * @date 2023-12-21 9:54
 * @email 1156388927@qq.com
 * @description
 */

public interface UserService {

    /***
     * 给用户充值余额
     * @param userId
     * @param count 充值的token数量
     * @return
     */
    String topUp(Integer userId,Integer count);

}
