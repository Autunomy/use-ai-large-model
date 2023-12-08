package com.hty.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hty.constant.Model;
import com.hty.eneity.pojo.ChatRequestParam;
import com.hty.service.ChatService;
import com.hty.utils.ChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.Map;

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

    //历史对话，需要按照user,assistant的顺序排列 使用队列方便控制上下文长度
    LinkedList<Map<String,String>> messages = new LinkedList<>();

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
        requestParam.setModel(Model.GPT_3_5_TURBO);

        //AI回复的JSON字符串
        String responseJSON = chatUtil.chat(requestParam);

        if(responseJSON == null){
            messages.removeLast();
            return "出错了，请重试";
        }

        //解析JSON字符串
        JSONObject responseJSONObject = JSON.parseObject(responseJSON);
        JSONArray choicesJSON = responseJSONObject.getJSONArray("choices");//包含AI的回复内容
        String usageJSON = responseJSONObject.getString("usage");//包含token的使用信息
        //解析token用量
        chatUtil.parseUsage(usageJSON);
        JSONObject choicesJSONObject = JSON.parseObject(choicesJSON.getString(0));
        String messageJSON = choicesJSONObject.getString("message");
        JSONObject messageJSONObject = JSON.parseObject(messageJSON);
        String content = messageJSONObject.getString("content");//最终解析的AI的回复

        //向消息列表中添加AI回复
        chatUtil.addAssistantQuestion(content,messages);

        return content;
    }
}
