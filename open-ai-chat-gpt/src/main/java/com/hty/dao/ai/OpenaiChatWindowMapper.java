package com.hty.dao.ai;

import com.hty.entity.pojo.OpenaiChatWindow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author hty
 * @date 2023-12-12 8:57
 * @email 1156388927@qq.com
 * @description
 */

@Mapper
public interface OpenaiChatWindowMapper {

    /***
     * 创建一个聊天窗口(上下文)
     * @param window
     */
    Integer createWindow(@Param("window")OpenaiChatWindow window);
}
