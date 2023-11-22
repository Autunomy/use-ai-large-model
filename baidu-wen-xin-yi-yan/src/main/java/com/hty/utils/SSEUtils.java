package com.hty.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hty
 * @date 2023-11-22 9:31
 * @email 1156388927@qq.com
 * @description
 */

@Slf4j
@Component
public class SSEUtils {

    /**
     * 这个用来保存用户与服务器之间的连接信息
     */
    private static final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    /***
     * 与服务器建立SSE连接
     * @param clientId
     * @return
     */
    public SseEmitter sseConnect(Long clientId){
        //已经连接过，直接返回连接
        if (sseEmitterMap.containsKey(clientId)) {
            return sseEmitterMap.get(clientId);
        }

        try {
            // 设置超时时间，0表示不过期。默认30秒
            SseEmitter sseEmitter = new SseEmitter(30 * 1000L);

            // 注册回调
            sseEmitter.onCompletion(completionCallBack(clientId));
            sseEmitter.onTimeout(timeoutCallBack(clientId));
            sseEmitterMap.put(clientId, sseEmitter);
            log.info("创建sse连接完成，当前客户端：{}", clientId);
            return sseEmitter;
        } catch (Exception e) {
            log.info("创建sse连接异常，当前客户端：{}", clientId);
        }
        return null;
    }

    /***
     * 发送消息
     * @param clientId
     * @param answer
     */
    public boolean sendMessage(Long clientId,String answer){
        SseEmitter sseEmitter = sseEmitterMap.get(clientId);
        if(sseEmitter == null){
            log.info("{}客户端的连接不存在",clientId);
            return false;
        }
        //SSE协议默认是以两个\n换行符为结束标志 需要在进行一次转义才能成功发送给前端
        try {
            sseEmitter.send(SseEmitter.event().data(answer.replace("\n","\\n")));
        } catch (IOException e) {
            log.error("{}客户端消息发送失败 => {}",clientId,e.getMessage());
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * 移除一个连接
     * @param clientId
     */
    public void removeConnect(Long clientId){
        sseEmitterMap.remove(clientId);
    }

    /**
     * 连接正常断开的方法
     * @param clientId
     * @return
     */
    private static Runnable completionCallBack(Long clientId) {
        return () -> {
            log.info("结束sse连接：{}", clientId);
            sseEmitterMap.remove(clientId);
        };
    }

    /**
     * 连接超时的断开方法
     * @param clientId
     * @return
     */
    private static Runnable timeoutCallBack(Long clientId) {
        return () -> {
            log.info("连接sse超时：{}", clientId);
            sseEmitterMap.remove(clientId);

        };
    }

}
