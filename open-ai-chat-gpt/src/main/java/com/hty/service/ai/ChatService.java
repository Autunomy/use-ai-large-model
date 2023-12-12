package com.hty.service.ai;

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
     * @return 包含AI回复和token用量详情的JSON
     */
    String chat(String question);

    /***
     * 设置前置指令
     * @param prompt 前置指令
     * @return
     */
    public void setPrompt(String prompt);

    /***
     * 流式问答接口
     * @param clientId
     * @param question
     * @param windowId 窗口的uuid
     */
    void streamChat(String question,Long clientId,String windowId);



    /***
     * 清空历史对话
     * TODO:为了方便测试开发的接口，需要删除
     */
    void clearHistory();

    /***
     * 创建一个聊天窗口
     * @param userId
     * @param modelId
     * @param prompt 前置的prompt
     * @return
     */
    String createChatWindow(Integer userId, Integer modelId,String prompt);
}
