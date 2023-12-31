package com.hty.service.ai;

import com.hty.entity.pojo.OpenaiChatHistoryMessage;
import com.hty.entity.pojo.OpenaiChatModel;

import java.util.List;

/**
 * @author hty
 * @date 2023-12-07 13:24
 * @email 1156388927@qq.com
 * @description
 */

public interface ChatService {

    /***
     * 非流式问答问答接口
     * @param question
     * @param windowId 聊天窗口id
     * @return 包含AI回复和token用量详情的JSON
     */
    String chat(String question,String windowId);

    /***
     * 设置前置指令
     * @param prompt 前置指令
     * @param windowId
     * @return
     */
    public void setPrompt(String prompt,String windowId);

    /***
     * 流式问答接口
     * @param clientId
     * @param question
     * @param windowId 窗口的uuid
     */
    void streamChat(String question,Long clientId,String windowId);


    /***
     * 创建一个聊天窗口
     * @param userId
     * @param modelId
     * @param prompt 前置的prompt
     * @return
     */
    String createChatWindow(Integer userId, Integer modelId,String prompt);


    /***
     * 获取一个窗口的全部消息
     * @param windowId
     * @return
     */
    List<OpenaiChatHistoryMessage> getAllMessage(String windowId);

    /***
     * 使用AI给聊天窗口生成标题
     * @param windowId 窗口id
     * @return 新的窗口标题
     */
    String generationTitle(String windowId,String question);

    /***
     * 获取所有的聊天模型
     * @return
     */
    List<OpenaiChatModel> getChatModelList();
}
