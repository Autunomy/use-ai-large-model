package com.hty.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hty.config.WenXinConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author hty
 * @date 2023-11-22 9:32
 * @email 1156388927@qq.com
 * @description
 */

@Component
@Slf4j
public class WenXinUtils {

    @Resource
    private OkHttpClient okHttpClient;
    @Resource
    private WenXinConfig wenXinConfig;

    //模型的一些默认值
    private final Double ERNIE_BOT_4_0_DEFAULT_TEMPERATURE = 0.8;
    private final Double ERNIE_BOT_4_0_DEFAULT_TOP_P = 0.8;
    private final Double ERNIE_BOT_4_0_DEFAULT_PENALTY_SCORE = 1.0;
    private final Boolean ERNIE_BOT_4_0_DEFAULT_STREAM = false;

    /**
     * 将问题使用OkHttp3发送给文心一言的ERNIE_Bot_4.0模型并获取返回的流
     * @param userId 用户id
     * @param temperature 较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定 默认0.8，范围 (0, 1.0]，不能为0
     *                    建议该参数和top_p只设置1个 建议top_p和temperature不要同时更改
     *        topP 影响输出文本的多样性，取值越大，生成文本的多样性越强 默认0.8，取值范围 [0, 1.0]
     *             建议该参数和temperature只设置1个 建议top_p和temperature不要同时更改，当前不使用topP参数
     * @param penaltyScore 通过对已生成的token增加惩罚，减少重复生成的现象。值越大表示惩罚越大，默认1.0，取值范围：[1.0, 2.0]
     * @param stream 是否是流式消息
     * @param messages 历史消息
     * @return
     */
    public Response getERNIEBot40ChatStream(Integer userId,
                                     Double temperature,
                                     Double penaltyScore,
                                     Boolean stream,
                                     LinkedList<Map<String,String>> messages){
        //构造请求参数的JSON格式
        String requestJson = constructRequestJson(userId,temperature,penaltyScore,stream,messages);
        //将请求参数封装为请求体
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
        //构造请求
        Request request = new Request.Builder()
                .url(wenXinConfig.ERNIE_Bot_4_0_URL + "?access_token=" + wenXinConfig.flushAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            // 检查响应是否成功
            if (response.isSuccessful()) {
                return response;
            } else {
                log.error("使用OkHttp访问文心一言请求成功但是响应不成功,响应结果:{}",response);
            }

        } catch (IOException e) {
            log.error("流式请求出错 => {}",e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 重载方法 将temperature、penaltyScore、stream设置为默认值
     * @param userId
     * @param messages
     * @return
     */
    public Response getERNIEBot40ChatStream(Integer userId,LinkedList<Map<String,String>> messages){
        return this.getERNIEBot40ChatStream(userId,ERNIE_BOT_4_0_DEFAULT_TEMPERATURE,ERNIE_BOT_4_0_DEFAULT_PENALTY_SCORE,ERNIE_BOT_4_0_DEFAULT_STREAM,messages);
    }

    /**
     * 重载方法 将temperature、penaltyScore设置为默认值
     * @param userId
     * @param messages
     * @param stream
     * @return
     */
    public Response getERNIEBot40ChatStream(Integer userId,LinkedList<Map<String,String>> messages,Boolean stream){
        return this.getERNIEBot40ChatStream(userId,ERNIE_BOT_4_0_DEFAULT_TEMPERATURE,ERNIE_BOT_4_0_DEFAULT_PENALTY_SCORE,stream,messages);
    }

    /**
     * 构造请求的请求参数
     * @param userId
     * @param temperature
     * @param penaltyScore
     * @param messages
     * @return
     */
    public String constructRequestJson(Integer userId,
                                       Double temperature,
                                       Double penaltyScore,
                                       boolean stream,
                                       LinkedList<Map<String, String>> messages) {
        Map<String,Object> request = new HashMap<>();
        request.put("user_id",userId.toString());
        request.put("temperature",temperature);
        request.put("penalty_score",penaltyScore);
        request.put("stream",stream);
        request.put("messages",messages);
        log.info("构造的请求JSON => {}",JSON.toJSONString(request));
        return JSON.toJSONString(request);
    }

    /**
     * 对资源的关闭 将前面资源释放的代码抽取出来，防止重复
     * @param response
     * @param responseBody
     * @param inputStream
     */
    public void closeStream(Response response, ResponseBody responseBody, InputStream inputStream){
        if(inputStream != null){
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(responseBody != null){
            responseBody.close();
        }
        if(response != null){
            response.close();
        }
    }

    /**
     * 将对话信息存在历史记录中
     * @param role 角色
     * @param content 内容
     */
    public void recordChatHistory(LinkedList<Map<String, String>> messages, String role, String content){
        HashMap<String, String> map = new HashMap<>();
        map.put("role",role);
        map.put("content",content);
        messages.offer(map);
        if(role.equals("assistant") && messages.size() > 4){
            int size = messages.size();
            while(size -- > 4){
                Map<String, String> message = messages.poll();
                log.info("从历史记录中清除的消息 => {}", JSON.toJSONString(message));
            }
        }
    }

    /**
     * 统计用户的Token消耗情况
     * @param answerJSON AI回复的JSON对象
     */
    public void countToken(JSONObject answerJSON){
        JSONObject usage = JSON.parseObject(answerJSON.getString("usage"));
        log.info("输入消耗{}tokens,输出消耗{}tokens,总共消耗{}tokens",
                usage.getString("prompt_tokens"),
                usage.getString("completion_tokens"),
                usage.getString("total_tokens"));
    }

    /***
     * 从消息记录中移除最后一次对话信息
     */
    public void removeMessage(LinkedList<Map<String,String>> messages){
        if(messages.peekLast() != null && messages.peekLast().get("role").equals("user")){
            messages.pollLast();
        }
    }

}
