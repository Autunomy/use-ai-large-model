package com.hty.dao.ai;

import com.hty.entity.pojo.OpenaiChatHistoryMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hty
 * @date 2023-12-13 16:24
 * @email 1156388927@qq.com
 * @description
 */

@Mapper
public interface OpenaiChatHistoryMessageMapper {

    /***
     * 从mysql中读取窗口中的消息,并且读取的时候只读取4对消息+一个前置提示词
     * @param windowId
     * @return
     */
    List<OpenaiChatHistoryMessage> getAllMessages(@Param("windowId") String windowId);

    /***
     * 插入一条消息
     * @param openaiChatHistoryMessage
     * @return
     */
    Integer insertMessage(@Param("openaiChatHistoryMessage") OpenaiChatHistoryMessage openaiChatHistoryMessage);


    /***
     * 统计一个窗口中的消息数量
     * @param windowId
     * @return
     */
    Long countWindowMessage(@Param("windowId") String windowId);
}
