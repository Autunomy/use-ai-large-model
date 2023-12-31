package com.hty.controller.ai;

import com.hty.entity.pojo.OpenaiChatHistoryMessage;
import com.hty.entity.pojo.OpenaiChatModel;
import com.hty.service.ai.ChatService;
import com.hty.utils.SSEUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.List;

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
    public String chat(String question,String windowId){
        return chatService.chat(question,windowId);
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
     * @param windowId
     * @return
     */
    @PostMapping("/set/prompt")
    public void setPrompt(String prompt,String windowId){
        chatService.setPrompt(prompt,windowId);
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
     * 获取一个窗口的全部消息
     * @param windowId
     * @return
     */
    @GetMapping("/get/all/message")
    public List<OpenaiChatHistoryMessage> getAllMessage(String windowId){
        return chatService.getAllMessage(windowId);
    }

    /***
     * 使用AI给聊天窗口生成标题
     * @param windowId 窗口id
     * @return 新的窗口标题
     */
    @PostMapping("/generation/title")
    public String generationTitle(String windowId,String question){
        return chatService.generationTitle(windowId,question);
    }

    /***
     * 获取所有的聊天模型
     * @return
     */
    @GetMapping("/chat/models")
    public List<OpenaiChatModel> getChatModelList(){
        return chatService.getChatModelList();
    }
}
