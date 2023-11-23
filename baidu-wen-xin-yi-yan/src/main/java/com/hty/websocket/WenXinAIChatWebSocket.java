package com.hty.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hty.config.WenXinConfig;
import com.hty.utils.WenXinUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

// 使用 @ServerEndpoint 注解表示此类是一个 WebSocket 端点
// 通过 value 注解，指定 websocket 的路径
@Slf4j
@Component
@ServerEndpoint(value = "/wen-xin/ai-websocket")
public class WenXinAIChatWebSocket {

    //由于WebSocket对象是多例的，所以不会注入进来，必须将其设置为static才能成功注入
    private static WenXinUtils wenXinUtils;
    private static WenXinConfig wenXinConfig;

    @Resource
    public void setWenXinUtils(WenXinUtils wenxinUtils){
        WenXinAIChatWebSocket.wenXinUtils = wenxinUtils;
    }

    @Resource
    public void setWenXinConfig(WenXinConfig wenXinConfig){
        WenXinAIChatWebSocket.wenXinConfig = wenXinConfig;
    }

    private Session session;
    //历史对话，需要按照user,assistant 使用队列方便控制上下文长度
    LinkedList<Map<String,String>> messages = new LinkedList<>();

    // 收到消息 message就是用户的问题
    @OnMessage
    public void onMessage(String message) throws IOException{
        log.info("[websocket] 收到消息：id={}，message={}", this.session.getId(), message);

        wenXinUtils.recordChatHistory(messages,"user",message);
        StringBuilder answer = new StringBuilder();
        // 发起异步请求
        Response response = wenXinUtils.getERNIEBot40ChatStream(1,messages,true, wenXinConfig.ERNIE_Bot_4_0_URL);
        InputStream inputStream = null;
        ResponseBody responseBody = null;
        // 发起异步请求
        try {
            responseBody = response.body();
            if (responseBody != null) {
                inputStream = responseBody.byteStream();
            }
            if(inputStream == null) {
                log.error("流获取失败");
                return;
            }
            // 以流的方式处理响应内容，输出到控制台 这里的数组大小一定不能太小，否则会导致接收中文字符的时候产生乱码
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String result = "";
                String str = new String(buffer, 0, bytesRead);
                //从6开始 因为有 data: 这个前缀 占了6个字符所以 0 + 6 = 6 结尾还需要截取2个字符，因为是以\n\n结尾
                JSONObject jsonObject = JSON.parseObject(str.substring(6, str.length()-2));
                if(jsonObject != null && jsonObject.getString("result") != null){
                    wenXinUtils.countToken(jsonObject);
                    result = jsonObject.getString("result");
                }
                sendMessage(result);
                answer.append(result);
            }
            wenXinUtils.recordChatHistory(messages,"assistant",answer.toString());
        } catch (IOException e) {
            log.error("流式请求出错 => {}",e.getMessage());
            //此处还需要移除当次的问题，因为向前端发送消息失败了，需要重新发送消息
            wenXinUtils.removeMessage(messages);
        }finally {
            wenXinUtils.closeStream(response,responseBody,inputStream);
        }

    }

    // 连接打开
    @OnOpen
    public void onOpen(Session session){
        // 保存 session 到对象
        this.session = session;
        log.info("[websocket] 新的连接：id={}", this.session.getId());
    }

    // 连接关闭
    @OnClose
    public void onClose(CloseReason closeReason){
        log.info("[websocket] 连接断开：id={}，reason={}", this.session.getId(),closeReason);
    }

    // 连接异常
    @OnError
    public void onError(Throwable throwable) throws IOException {
        log.info("[websocket] 连接异常：id={}，throwable={}", this.session.getId(), throwable.getMessage());
    }

    /***
     * 给前端发送消息
     * @param message
     */
    public void sendMessage(String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}