package com.hty.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hty.config.WenXinConfig;
import com.hty.constant.WenXinModel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author hty
 * @date 2023-11-23 21:45
 * @email 1156388927@qq.com
 * @description 文心大模型图片生成工具类 目前只有一个模型：Stable-Diffusion-XL
 */

@Component
@Slf4j
public class WenXinImgUtils {
    @Resource
    private OkHttpClient okHttpClient;
    @Resource
    private WenXinConfig wenXinConfig;
    @Resource
    private WenXinChatUtils wenXinChatUtils;

    private final String[] sizeArray = new String[]{"768x768", "768x1024", "1024x768", "576x1024", "1024x576", "1024x1024"};

    /**
     * 生成图像
     * @param prompt 提示词(必填) 长度限制为1024字符，建议中文或者英文单词总数量不超过150个
     * @param negativePrompt 反向提示词，即用户希望图片不包含的元素。长度限制为1024字符，建议中文或者英文单词总数量不超过150个
     * @param sizeArrayIndex 生成图片长宽，默认值 1024x1024 传过来的是一个下标，需要从sizeArray中获取
     * @param n 生成图片数量  默认值为1 取值范围为1-4 单次生成的图片较多及请求较频繁可能导致请求超时
     * @return 最终相应的JSON字符串
     */
    public String generationImg(String prompt,
                                  String negativePrompt,
                                  Integer sizeArrayIndex,
                                  Integer n){
        if(prompt == null || prompt.length() >= 1024){
            log.error("提示词过长或提示词为空无法生成图片");
            return null;
        }

        if(negativePrompt != null && negativePrompt.length() >= 1024){
            log.error("反向提示词过长无法生成图片");
            return null;
        }

        if(n == null || n <= 0 || n > 4){
            //数量错误或为null置为默认值
            n = 1;
        }

        if(sizeArrayIndex == null || sizeArrayIndex < 0 || sizeArrayIndex >= sizeArray.length){
            //如果下标为null或不在范围内就设置为默认值(最大的大小)
            sizeArrayIndex = sizeArray.length - 1;
        }

        String responseJson = null;
        //先获取令牌然后才能访问api
        if (wenXinConfig.flushAccessToken() != null) {
            String requestBodyJSON = constructRequestJson(prompt, negativePrompt, sizeArray[sizeArrayIndex], n);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBodyJSON);

            Request request = new Request.Builder()
                    .url(WenXinModel.getUrl(WenXinModel.Stable_Diffusion_XL) + "?access_token=" + wenXinConfig.flushAccessToken())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            try {
                responseJson = client.newCall(request).execute().body().string();
                //将回复的内容转为一个JSONObject
                JSONObject responseObject = JSON.parseObject(responseJson);
                //统计Token的消耗
                wenXinChatUtils.countToken(responseObject);
            } catch (IOException e) {
                log.error("网络有问题");
                return "网络有问题，请稍后重试";
            }
        }
        return responseJson;
    }

    /***
     * 构造请求的请求参数
     * @param prompt 提示词(必填) 长度限制为1024字符，建议中文或者英文单词总数量不超过150个
     * @param negativePrompt 反向提示词，即用户希望图片不包含的元素。长度限制为1024字符，建议中文或者英文单词总数量不超过150个
     * @param size 生成图片长宽，默认值 1024x1024，取值范围如下
     * @param n 生成图片数量  默认值为1 取值范围为1-4 单次生成的图片较多及请求较频繁可能导致请求超时
     * @return 构造好的请求参数JSON
     */
    public String constructRequestJson(String prompt,
                                       String negativePrompt,
                                       String size,
                                       Integer n) {
        Map<String,Object> request = new HashMap<>();
        request.put("prompt",prompt);
        if(negativePrompt != null) request.put("negative_prompt",negativePrompt);
        request.put("size",size);
        request.put("n",n);
        log.info("构造的请求JSON => {}", JSON.toJSONString(request));
        return JSON.toJSONString(request);
    }

}
