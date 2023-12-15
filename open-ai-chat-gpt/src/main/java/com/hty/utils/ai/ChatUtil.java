package com.hty.utils.ai;

import com.alibaba.fastjson.JSON;
import com.hty.config.OpenAIConfig;
import com.hty.constant.ChatModel;
import com.hty.constant.RequestURL;
import com.hty.dao.ai.OpenaiChatModelMapper;
import com.hty.entity.ai.ChatRequestParam;
import com.hty.entity.ai.Usage;
import com.hty.entity.pojo.OpenaiChatModel;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.ModelType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

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
    @Resource
    private EncodingRegistry registry;
    @Resource
    private OpenaiChatModelMapper openaiChatModelMapper;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    /***
     * 一个最普通的非流式请求接口，提交问题并返回结果
     * @param question
     * @return
     */
    public String chat(String question,String model){
        ChatRequestParam requestParam = new ChatRequestParam();
        LinkedList<Map<String,String>> messages = new LinkedList<>();
        Map<String,String> map = new HashMap<>();
        map.put("role","user");
        map.put("content",question);
        messages.add(map);

        requestParam.setMessages(messages);
        requestParam.setModel(model);
        String answer = "";
        try (Response response = chat(requestParam);
        ResponseBody responseBody = response.body();){
            if(responseBody != null){
                answer = responseBody.string();
            }
        } catch (IOException e) {
            log.info("请求出错 => {}",e.getMessage());
        }

        return answer;
    }

    /***
     * 问答接口
     * @param requestParam
     * @return
     */
    public Response chat(ChatRequestParam requestParam){

        if(!checkTokenCount(requestParam)){
            log.info("消息长度过长，请重新提问");
            return null;
        }

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

    /***
     * 判断请求的token是否大于模型的token，超过长度的话就将message进行弹出
     * @param requestParam
     * @return
     */
    private Boolean checkTokenCount(ChatRequestParam requestParam){
        Usage usage = computePromptToken(requestParam, null);
        OpenaiChatModel openaiChatModel = openaiChatModelMapper.selectModelByName(requestParam.getModel());
        while(openaiChatModel.getMaxTokens() < usage.getPromptTokens()){
            //因为会有system输入，所以当只剩下一条消息的时候说明已经没有了上下文
            if(requestParam.getMessages().size() == 1) return false;
            //删除2遍的意思就是删除输入的同时也要删除输出
            requestParam.getMessages().remove(1);
            requestParam.getMessages().remove(1);
            //重新计算token消耗
            usage = computePromptToken(requestParam, null);
        }
        return true;
    }

    /**
     * 构造请求的请求参数
     */
    private String constructRequestJson(ChatRequestParam requestParam) {
        Map<String,Object> request = new HashMap<>();
        //对于必须的字段需要增加非空判断
        if(requestParam.getModel() == null || requestParam.getMessages() == null){
            log.error("请求缺少参数,model => {},messages => {}",requestParam.getModel(),requestParam.getMessages());
            throw new RuntimeException("请求缺少参数");
        }
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
     * 使用jtokkit库来计算token消耗
     * @param requestParam
     * @param answer
     * @return 返回的是一个usage对象
     *
     * 当前统计方式中,每次都会比真实略微高一点,这样可以从中去赚取差价
     */
    public Usage computePromptToken(ChatRequestParam requestParam, String answer){
        //获取模型对应的编码方式
        Encoding encoding = registry.getEncodingForModel(ModelType.fromName(requestParam.getModel()).get());
        //拼接输入,方式:将所有角色部分,content部分,model部分放入一个字符串中
        StringBuilder content = new StringBuilder();
        for (Map<String, String> message : requestParam.getMessages()) {
            content.append("role").append(message.get("role"));
            content.append(" ");
            content.append("content").append(message.get("content"));
            content.append(" ");
        }
        content.append("model").append(requestParam.getModel());
        //使用获得到的编码方式计算token数量并设置为usage对象
        Usage usage = new Usage();
        usage.setPromptTokens(encoding.countTokens(content.toString()));
        usage.setCompletionTokens(encoding.countTokens(answer));
        usage.countTotalTokens();
        return usage;
    }

    /***
     * 根据content计算token消耗
     * @param content
     * @return
     */
    public Integer computeToken(String content,String model){
        //获取模型对应的编码方式
        Encoding encoding = registry.getEncodingForModel(ModelType.fromName(model).get());
        return encoding.countTokens(content);
    }

    /***
     * 从数据库中加载所有没有过期的模型到redis中
     * TODO:需要加锁
     */
    public void loadModelFromDatabase2Redis(){
        //获取所有的聊天的model
        stringRedisTemplate.delete("chatModelSet");
        List<OpenaiChatModel> openaiChatModelList = openaiChatModelMapper.selectAllModel();
        for (OpenaiChatModel model : openaiChatModelList) {
            //使用set存储，方便进行containsKey操作
            stringRedisTemplate.opsForHash().put("chatModelSet",model.getName(),JSON.toJSONString(model));
        }

        //TODO:获取image生成、音频等的model
    }

    /***
     *
     * @return 聊天模型列表
     */
    public List<OpenaiChatModel> getAllChatModel(){
        List<OpenaiChatModel> modelList = new ArrayList<>();
        //从redis中获取模型列表
        List<Object> chatModelList = stringRedisTemplate.opsForHash().values("chatModelSet");
        //为空就重新从数据库中加载
        if (chatModelList.size() == 0){
            loadModelFromDatabase2Redis();
        }
        for (Object modelJSON : chatModelList) {
            modelList.add(JSON.parseObject(modelJSON.toString(),OpenaiChatModel.class));
        }
        return modelList;
    }
}
