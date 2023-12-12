package com.hty.controller.ai;

import com.hty.service.ai.ChatService;
import com.hty.utils.SSEUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
     * @param windowId 窗口的UUID
     */
    @PostMapping(value = "/sse/chat")
    public void streamOutputToPage(Long clientId,String question,String windowId){
        chatService.streamChat(question,clientId,windowId);
    }

    /***
     * 设置最开始system角色的提示词,专门用于
     * @param prompt
     * @return
     */
    @PostMapping("/set/prompt")
    public void setPrompt(String prompt){
        chatService.setPrompt(prompt);
    }

    /***
     * 创建一个聊天窗口
     * @param userId
     * @param modelId
     * @param prompt 前置的prompt提示词
     * @return
     */
    @PostMapping("/create/chat/window")
    public String createChatWindow(Integer userId,Integer modelId,String prompt){
        return chatService.createChatWindow(userId,modelId,prompt);
    }

    /***
     * 清空历史对话
     * TODO:此controller是用于开发过程中的测试方便，开发完成后应该删除
     */
    @GetMapping("/clear/history")
    public void clearHistory(){
        chatService.clearHistory();
    }
}
