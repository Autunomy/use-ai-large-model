package com.hty.service;

import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author hty
 * @date 2023-11-23 9:10
 * @email 1156388927@qq.com
 * @description
 */

public interface AIChatService {

    /**
     * 非流式问答
     * @param question 用户的问题
     * @return ai的回复 是一个JSON字符串
     */
    public String unStreamChat(String question);

    /**
     * 流式API同步调用,调用的效果和非流式相同,因为是在后端进行字符串拼接之后再一次性返回给前端
     * @param question 用户的问题
     * @return ai的回复 是一个JSON字符串
     */
    public String streamOutputToTerminal(String question);

    /**
     * SSE方式向前端发送消息
     * @param clientId 用户的唯一标识
     * @param question 用户的问题
     */
    public void sendMessageToPageBySSE(Long clientId,String question);
}
