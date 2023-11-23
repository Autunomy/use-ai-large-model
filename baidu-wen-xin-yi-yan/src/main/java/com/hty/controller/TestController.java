package com.hty.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hty.config.WenXinConfig;
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
public class TestController {

    @Resource
    private WenXinConfig wenXinConfig;
    @Resource
    private WenxinUtils wenxinUtils;
    @Resource
    private SSEUtils sseUtils;
    /**
     * 用来异步发送消息
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    //TODO:这里可能需要的是一个队列，始终让队列中保持固定的元素，防止上下文太多导致消耗太多的token
    //历史对话，需要按照user,assistant
    Queue<Map<String,String>> messages = new ArrayDeque<>();

    /**
     * 非流式问答
     * @param question 用户的问题
     * @return ai的回复
     */
    @PostMapping("/test1")
    public String unStreamChat(String question) {
        if(question == null || question.equals("")){
            return "请输入问题";
        }
        String responseJson = null;
        //先获取令牌然后才能访问api
        if (wenXinConfig.flushAccessToken() != null) {
            recordChatHistory("user",question);
            String requestJson = wenxinUtils.constructRequestJson(1,0.95,1.0,false,messages);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
            Request request = new Request.Builder()
                    .url(wenXinConfig.ERNIE_Bot_4_0_URL + "?access_token=" + wenXinConfig.flushAccessToken())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            try {
                responseJson = client.newCall(request).execute().body().string();
                //将回复的内容转为一个JSONObject
                JSONObject responseObject = JSON.parseObject(responseJson);
                //将回复的内容添加到消息中
                recordChatHistory("assistant",responseObject.getString("result"));
            } catch (IOException e) {
                log.error("网络有问题");
                return "网络有问题，请稍后重试";
            }
        }
        return responseJson;
    }

    /**
     * 将对话信息存在历史记录中
     * @param role 角色
     * @param content 内容
     */
    void recordChatHistory(String role,String content){
        HashMap<String, String> map = new HashMap<>();
        map.put("role",role);
        map.put("content",content);
        messages.offer(map);
        if(role.equals("assistant") && messages.size() > 4){
            int size = messages.size();
            while(size -- > 4){
                Map<String, String> message = messages.poll();
                log.info("从历史记录中清除的消息 => {}",JSON.toJSONString(message));
            }
        }
    }

    /**
     * 流式回答 输出到控制台中
     * @return
     */
    @PostMapping("/test2")
    public String streamOutputToTerminal(String question){
        //将问题放在历史对话中
        recordChatHistory("user",question);
        StringBuilder answer = new StringBuilder();
        // 发起异步请求
        Response response = wenxinUtils.getERNIEBot40ChatStream(1,messages,true);
        InputStream inputStream = null;
        ResponseBody responseBody = null;
        // 以流的方式处理响应内容，输出到控制台
        byte[] buffer = new byte[2048];
        int bytesRead;
        try {
            responseBody = response.body();
            if (responseBody != null) {
                inputStream = responseBody.byteStream();
            }
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // 在控制台输出每个数据块
                System.out.write(buffer, 0, bytesRead);
                //将结果汇总起来  这里new出来的字符串中是包含id、token消耗数量等信息的JSON字符串，需要做信息统计的话可以在这里进行拓展
                answer.append(new String(buffer, 0, bytesRead));
            }
        } catch (IOException e) {
            //TODO:如果出现了异常就应该将问题也从对话历史中删除
            log.error("InputStream流式读取出错 => {}",e.getMessage());
            throw new RuntimeException(e);
        }finally {
            closeStream(response,responseBody,inputStream);
        }

        //将回复的内容添加到消息中
        StringBuilder assistantAnswer = new StringBuilder();
        String[] answerArray = answer.toString().split("data: ");
        for (int i=1;i<answerArray.length;++i) {
            answerArray[i] = answerArray[i].substring(0,answerArray[i].length() - 2);
            assistantAnswer.append(JSON.parseObject(answerArray[i]).get("result"));
        }
        recordChatHistory("assistant",assistantAnswer.toString());
        return assistantAnswer.toString();
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
        //异步发送消息
        executorService.execute(() -> {
            recordChatHistory("user",question);

            StringBuilder answer = new StringBuilder();
            // 发起异步请求
            Response response = wenxinUtils.getERNIEBot40ChatStream(1,messages,true);
            InputStream inputStream = null;
            ResponseBody responseBody = null;
            // 发起异步请求
            try {
                responseBody = response.body();
                if (responseBody != null) {
                    inputStream = responseBody.byteStream();
                }
                // 以流的方式处理响应内容，输出到控制台 这里的数组大小一定不能太小，否则会导致接收中文字符的时候产生乱码
                byte[] buffer = new byte[2048];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String result = "";
                    //TODO:这里的字符串中包含有token消耗、id等信息，若需要数据统计则可以在这里进行拓展
                    String str = new String(buffer, 0, bytesRead);
                    //从6开始 因为有 data: 这个前缀 占了6个字符所以 0 + 6 = 6 结尾还需要截取2个字符，因为是以\n\n结尾
                    JSONObject jsonObject = JSON.parseObject(str.substring(6, str.length()-2));
                    if(jsonObject != null && jsonObject.getString("result") != null){
                        result = jsonObject.getString("result");
                    }
                    if(!sseUtils.sendMessage(clientId,result)) {
                        log.error("消息发送失败了，结束消息发送");
                        break;
                    }
                    answer.append(result);
                }
                recordChatHistory("assistant",answer.toString());
            } catch (IOException e) {
                log.error("流式请求出错,断开与{}的连接 => {}",clientId,e.getMessage());
                //移除当前的连接
                sseUtils.removeConnect(clientId);
                //TODO:此处还需要移除当次的问题，因为向前端发送消息失败了，需要重新发送消息
            }finally {
                closeStream(response,responseBody,inputStream);
            }
        });
    }

    /**
     * 对资源的关闭 将前面资源释放的代码抽取出来，防止重复
     * @param response
     * @param responseBody
     * @param inputStream
     */
    private void closeStream(Response response,ResponseBody responseBody,InputStream inputStream){
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
}