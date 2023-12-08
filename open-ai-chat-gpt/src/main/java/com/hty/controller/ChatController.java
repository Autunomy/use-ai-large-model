package com.hty.controller;

import com.hty.service.ChatService;
import com.hty.utils.SSEUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

/**
 * @author hty
 * @date 2023-12-07 13:23
 * @email 1156388927@qq.com
 * @description
 */

@RestController
@Slf4j
@RequestMapping("/openai")
public class ChatController {

    @Resource
    private ChatService chatService;
    @Resource
    private SSEUtils sseUtils;

    /***
     * 非流式问答接口
     * @param question
     * @return
     */
    @PostMapping("/chat")
    public String chat(String question){
        return chatService.chat(question);
    }

    /**
     * 与前端建立SSE连接
     * @param clientId 客户端ID
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
        chatService.streamChat(question,clientId);
    }
}
