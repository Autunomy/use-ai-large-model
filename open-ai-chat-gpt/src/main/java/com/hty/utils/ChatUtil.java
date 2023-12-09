package com.hty.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hty.config.OpenAIConfig;
import com.hty.constant.RequestURL;
import com.hty.eneity.pojo.ChatRequestParam;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
    @Resource
    private OpenAIConfig openAIConfig;

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
                .addHeader("Authorization", "Bearer " + openAIConfig.apiKey)
                .build();
        OkHttpClient client = okHttpClient.newBuilder().build();
        String responseJson = null;
        try {
            ResponseBody responseBody = client.newCall(request).execute().body();
            if(responseBody != null){
                responseJson = responseBody.string();
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
     * @param requestParam
     * @return
     */
    public Response streamChat(ChatRequestParam requestParam){
        //构造请求的JSON字符串
        String requestJson = constructRequestJson(requestParam);
        //构造请求体
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
        //构造请求
        Request request = new Request.Builder()
                .url(RequestURL.PROXY_CHAT_URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+openAIConfig.apiKey)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            // 检查响应是否成功
            if (response.isSuccessful()) {
                return response;
            } else {
                log.error("使用OkHttp访问ChatGPT请求成功但是响应不成功,响应结果:{}",response);
            }

        } catch (IOException e) {
            log.error("流式请求出错 => {}",e.getMessage());
            throw new RuntimeException(e);
        }
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

        //如果超过了4对问答就将最前面的全部删除
        int n = messages.size() - 8;
        Iterator<Map<String, String>> iterator = messages.iterator();
        if(messages.getFirst().get("role").equals("system")){
            iterator.next();
            n -= 1;
        }
        while(n -- > 0){
            iterator.remove();
        }
    }


    /***
     * 根据content计算token消耗
     * @param content
     * @return
     */
    public Integer computeToken(String content){
        int tokenCount = 0;

        // 分别处理中文和英文
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            // 如果字符是中文（基于字符范围），视为一个 token
            if (ch >= 0x4E00 && ch <= 0x9FFF) {
                tokenCount++;
            }
            // 对于非中文字符，以空格为分割来估算英文单词数量
            else if (ch == ' ') {
                tokenCount++;
            }

            // 考虑连续的英文单词或非中文字符
            if (i == content.length() - 1 && ch != ' ') {
                tokenCount++;
            }
        }

        return tokenCount;
    }
}
