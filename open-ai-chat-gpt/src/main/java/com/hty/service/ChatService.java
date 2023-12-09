package com.hty.service;

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
     */
    void streamChat(String question,Long clientId);


    /***
     * 清空历史对话
     */
    void clearHistory();

}
