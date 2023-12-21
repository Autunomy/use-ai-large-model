package com.hty.dao.user;

import com.hty.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author hty
 * @date 2023-12-17 11:50
 * @email 1156388927@qq.com
 * @description
 */

@Mapper
public interface UserMapper {

    /***
     * 根据窗口id查询用户余额
     * @param windowId
     * @return
     */
    User selectUserLastByWindowId(@Param("windowId") String windowId);

    /***
     * 根据用户id更新账户余额
     * @param user
     * @return
     */
    Integer updateUserBillLastById(@Param("user") User user);

    /***
     * 给用户充值
     * @param userId
     * @param count 充值的token数量
     * @return
     */
    Integer updateUserBillLast(@Param("userId") Integer userId,@Param("count") Integer count);

}
