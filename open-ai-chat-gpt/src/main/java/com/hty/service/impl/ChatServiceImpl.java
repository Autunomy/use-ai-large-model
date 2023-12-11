package com.hty.service.impl;

import com.alibaba.fastjson.JSON;
import com.hty.constant.ChatModel;
import com.hty.entity.ai.ChatRequestParam;
import com.hty.entity.ai.ChatResponseBody;
import com.hty.entity.ai.StreamChatResponseBody;
import com.hty.service.ChatService;
import com.hty.utils.ChatUtil;
import com.hty.utils.SSEUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hty
 * @date 2023-12-07 13:24
 * @email 1156388927@qq.com
 * @description
 */

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatUtil chatUtil;
    @Resource
    private SSEUtils sseUtils;

    //历史对话，需要按照user,assistant的顺序排列 使用队列方便控制上下文长度
    LinkedList<Map<String,String>> messages = new LinkedList<>();

    //用来异步发送消息
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public String chat(String question) {
        if(question == null || question.equals("")){
            log.info("用户请求过来的问题为空");
            return null;
        }

        chatUtil.addUserQuestion(question,messages);

        //设置请求的参数信息(聊天的配置信息)
        ChatRequestParam requestParam = new ChatRequestParam();
        requestParam.setMessages(messages);
        requestParam.setModel(ChatModel.GPT_3_5_TURBO);

        //AI回复的JSON字符串
        String responseJSON = chatUtil.chat(requestParam);

        if(responseJSON == null){
            messages.removeLast();
            return "出错了，请重试";
        }

        //解析JSON字符串
        ChatResponseBody chatResponseBody = JSON.parseObject(responseJSON, ChatResponseBody.class);

        //向消息列表中添加AI回复
        String content = chatResponseBody.getChoices()[0].getMessage().getContent();
        chatUtil.addAssistantQuestion(content,messages);


        return content;
    }

    @Override
    public void setPrompt(String prompt) {
        Map<String,String> systemPrompt = new HashMap<>();
        systemPrompt.put("role","system");
        systemPrompt.put("content",prompt);
        messages.addFirst(systemPrompt);
        log.info("设置prompt => {}",prompt);
    }

    @Override
    public void streamChat(String question,Long clientId) {
        if(question == null || question.equals("")){
            log.info("用户请求过来的问题为空");
            return;
        }

        //异步发送消息
        executorService.execute(() -> {
            //记录消息
            chatUtil.addUserQuestion(question,messages);

            //设置请求的参数信息(聊天的配置信息)
            ChatRequestParam requestParam = new ChatRequestParam();
            requestParam.setMessages(messages);
            requestParam.setModel(ChatModel.GPT_3_5_TURBO);
            requestParam.setStream(true);

            // 发起异步请求
            Response response = chatUtil.streamChat(requestParam);
            if(response == null){
                messages.removeLast();
                return;
            }

            BufferedReader reader = null;
            ResponseBody responseBody = null;
            // 发起异步请求
            try {
                responseBody = response.body();
                if (responseBody == null) {
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                String str;
                //最终的回答
                StringBuilder answer = new StringBuilder();
                //因为一条消息内容太少，所以进行消息合并然后发送
                StringBuilder sseBuffer = new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    //解析每条数据 最终结束标志为 data: [DONE]
                    if(!str.equals("data: [DONE]")){
                        //前面有data前缀
                        StreamChatResponseBody streamChatResponseBody = JSON.parseObject(str.substring(6), StreamChatResponseBody.class);
                        String content = streamChatResponseBody.getChoices()[0].getDelta().getContent();
                        if(content != null){
                            //增加一个缓冲区,因为ChatGPT流式回答中每条内容太少，会产生过多的请求
                            if(sseBuffer.length() > 20){
                                if(!sseUtils.sendMessage(clientId,sseBuffer.toString())){
                                    log.error("消息发送失败了，结束消息发送,失败的消息 => {}",sseBuffer);
                                    break;
                                }
                                sseBuffer = new StringBuilder();
                            }
                            answer.append(content);
                            sseBuffer.append(content);
                        }
                    }
                    //由于每条消息后面还有一个换行，需要将换行读取掉然后再继续读取下一条消息
                    reader.readLine();
                }
                //如果缓冲区还有没有发送的数据需要再次发送
                if(sseBuffer.length() != 0){
                    if(!sseUtils.sendMessage(clientId,sseBuffer.toString())){
                        log.error("消息发送失败了，结束消息发送,失败的消息 => {}",sseBuffer);
                    }
                }

                chatUtil.addAssistantQuestion(answer.toString(),messages);
                //统计token消耗
                log.info("本次请求输入消耗{}tokens,输出消耗{}tokens,总计消耗{}tokens",
                        chatUtil.computeToken(question),
                        chatUtil.computeToken(answer.toString()),
                        chatUtil.computeToken(question) + chatUtil.computeToken(answer.toString()));


            } catch (IOException e) {
                log.error("流式请求出错,断开与{}的连接 => {}",clientId,e.getMessage());
                //移除当前的连接
                sseUtils.removeConnect(clientId);
                //此处还需要移除当次的问题，因为向前端发送消息失败了，需要重新发送消息
                messages.removeLast();
            }finally {
                try {
                    if(reader != null)
                        reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(responseBody != null)
                    responseBody.close();
                response.close();
            }
        });
    }

    @Override
    public void clearHistory() {
        log.info("清空历史对话列表");
        messages.clear();
    }
}
