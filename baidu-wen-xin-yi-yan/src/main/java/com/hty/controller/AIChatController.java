package com.hty.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hty.config.WenXinConfig;
import com.hty.service.AIChatService;
import com.hty.utils.SSEUtils;
import com.hty.utils.WenxinUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hty
 * @date 2023-11-18 9:46
 * @email 1156388927@qq.com
 * @description
 */

@RestController
@Slf4j
public class AIChatController {
    @Resource
    private SSEUtils sseUtils;

    @Resource
    private AIChatService aiChatService;

    /**
     * 非流式问答
     * @param question 用户的问题
     * @return ai的回复
     */
    @PostMapping("/chat")
    public String unStreamChat(String question) {
        return aiChatService.unStreamChat(question);
    }

    /**
     * 流式回答 输出到控制台中
     * @return 返回JSON字符串，只包含回答的内容
     */
    @PostMapping("/chat-stream")
    public String streamOutputToTerminal(String question){
        return aiChatService.streamOutputToTerminal(question);
    }

    /**
     * 与前端建立SSE连接
     * @param clientId
     * @return
     */
    @GetMapping(value = "/sse/connect", produces="text/event-stream;charset=UTF-8")
    public SseEmitter sseConnect(Long clientId){
        return sseUtils.sseConnect(clientId);
    }

    /**
     * SSE方式向前端发送消息
     * @param clientId
     * @param question
     */
    @PostMapping(value = "/sse/chat")
    public void streamOutputToPage(Long clientId,String question){
        aiChatService.sendMessageToPageBySSE(clientId,question);
    }

}