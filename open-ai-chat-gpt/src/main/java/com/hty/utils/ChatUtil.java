package com.hty.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hty.constant.RequestURL;
import com.hty.eneity.pojo.ChatRequestParam;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author hty
 * @date 2023-12-07 13:07
 * @email 1156388927@qq.com
 * @description
 */

@Slf4j
@Component
public class ChatUtil {
    @Resource
    private OkHttpClient okHttpClient;

    @Value("${openai.apikey}")
    private String apiKey;

    /***
     * 非流式请求接口
     * @param requestParam 请求参数封装的实体类
     * @return AI返回的JSON回答
     */
    public String chat(ChatRequestParam requestParam){
        //构造请求的JSON字符串
        String requestJson = constructRequestJson(requestParam);
        //构造请求体
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
        //构造请求
        Request request = new Request.Builder()
                .url(RequestURL.PROXY_CHAT_URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+apiKey)
                .build();
        OkHttpClient client = okHttpClient.newBuilder().build();
        String responseJson = null;
        try {
            ResponseBody responseBody = client.newCall(request).execute().body();
            if(responseBody != null){
                responseJson = responseBody.string();
//                //将回复的内容转为一个JSONObject
//                JSONObject responseObject = JSON.parseObject(responseJson);
//                log.info("AI返回的回答JSON => {}",responseObject);
            }else{
                log.info("AI返回的回答为空");
            }

        } catch (IOException e) {
            log.error("请求发起失败 => {}",e.getMessage());
        }
        return responseJson;
    }

    /***
     * 流式问答接口(SSE方式)
     * @param chatRequestParam
     * @return
     */
    public String streamChat(ChatRequestParam chatRequestParam){


        return null;
    }

    /**
     * 构造请求的请求参数
     */
    private String constructRequestJson(ChatRequestParam requestParam) {
        Map<String,Object> request = new HashMap<>();
        request.put("model",requestParam.getModel());
        request.put("messages",requestParam.getMessages());

        if(requestParam.getTemperature() != null){
            request.put("temperature",requestParam.getTemperature());
        } else if(requestParam.getTopP() != null){
            request.put("top_p",requestParam.getTopP());
        }

        if(requestParam.getN() != null) request.put("n",requestParam.getN());
        if(requestParam.getStream() != null) request.put("stream",requestParam.getStream());
        if(requestParam.getStop() != null) request.put("stop",requestParam.getStop());
        if(requestParam.getMaxTokens() != null) request.put("max_tokens",requestParam.getMaxTokens());
        if(requestParam.getPresencePenalty() != null) request.put("presence_penalty",requestParam.getPresencePenalty());
        if(requestParam.getFrequencyPenalty() != null) request.put("frequency_penalty",requestParam.getFrequencyPenalty());
        if(requestParam.getLogitBias() != null) request.put("logit_bias",requestParam.getLogitBias());
        if(requestParam.getUser() != null) request.put("user",requestParam.getUser());

        log.info("构造的请求JSON => {}", JSON.toJSONString(request));
        return JSON.toJSONString(request);
    }

    /***
     * 向消息列表中添加用户的问题
     * @param question
     * @param messages
     */
    public void addUserQuestion(String question,LinkedList<Map<String, String>> messages){
        Map<String,String> map = new HashMap<>();
        map.put("role","user");
        map.put("content",question);
        messages.addLast(map);
    }

    /***
     * 向消息列表中添加AI的回复
     * @param content
     * @param messages
     */
    public void addAssistantQuestion(String content,LinkedList<Map<String, String>> messages){
        Map<String,String> map = new HashMap<>();
        map.put("role","assistant");
        map.put("content",content);
        messages.addLast(map);
    }

    /***
     * 解析token的用量
     * @param usageJSON
     */
    public void parseUsage(String usageJSON){
        JSONObject usageJSONObject = JSON.parseObject(usageJSON);
        String promptTokens = usageJSONObject.getString("prompt_tokens");
        String completionTokens = usageJSONObject.getString("completion_tokens");
        String totalTokens = usageJSONObject.getString("total_tokens");
        log.info("本次请求输入消耗{}tokens,输出消耗{}tokens,总计消耗{}tokens",promptTokens,completionTokens,totalTokens);
    }

}
