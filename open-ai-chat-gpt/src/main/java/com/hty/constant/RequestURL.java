package com.hty.constant;

/**
 * @author hty
 * @date 2023-12-07 13:04
 * @email 1156388927@qq.com
 * @description 各种类型模型的请求地址
 */

public class RequestURL {
    //对话的URL
    public static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";
    public static final String PROXY_CHAT_URL = "https://openai-proxy-gk5q.onrender.com/v1/chat/completions";

    //图片生成的URL
    public static final String GENERATION_IMAGE = "https://api.openai.com/v1/images/generations";
    public static final String PROXY_GENERATION_IMAGE = "https://openai-proxy-gk5q.onrender.com/v1/images/generations";
}
