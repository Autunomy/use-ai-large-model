package com.hty.utils.ai;

import com.alibaba.fastjson.JSON;
import com.hty.config.OpenAIConfig;
import com.hty.constant.RequestURL;
import com.hty.entity.ai.GenerationImageParam;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hty
 * @date 2023-12-08 15:11
 * @email 1156388927@qq.com
 * @description
 */

@Component
@Slf4j
public class ImageUtil {

    @Resource
    private OpenAIConfig openAIConfig;
    @Resource
    private OkHttpClient okHttpClient;

    public String generationImage(GenerationImageParam generationImageParam){
        //构造请求的JSON字符串
        String requestJson = constructRequestJson(generationImageParam);
        //构造请求体
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
        //构造请求
        Request request = new Request.Builder()
                .url(RequestURL.PROXY_GENERATION_IMAGE)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+openAIConfig.apiKey)
                .build();
        OkHttpClient client = okHttpClient.newBuilder().build();

        String responseJson = null;
        try {
            ResponseBody responseBody = client.newCall(request).execute().body();
            if(responseBody != null){
                responseJson = responseBody.string();
            }else{
                log.info("AI返回的图片为空");
            }

        } catch (IOException e) {
            log.error("请求发起失败 => {}",e.getMessage());
        }
        return responseJson;
    }


    /**
     * 构造请求的请求参数
     */
    private String constructRequestJson(GenerationImageParam generationImageParam) {
        Map<String,Object> request = new HashMap<>();
        request.put("prompt",generationImageParam.getPrompt());
        request.put("model",generationImageParam.getModel());

        if(generationImageParam.getN() != null) request.put("n",generationImageParam.getN());
        if(generationImageParam.getQuality() != null && generationImageParam.getModel().equals("dall-e-3"))
            request.put("quality",generationImageParam.getQuality());
        if(generationImageParam.getResponseFormat() != null)
            request.put("response_format",generationImageParam.getResponseFormat());
        //将size信息进行更新
        generationImageParam.updateSizeBySizeIdx();
        if(generationImageParam.getSize() != null) request.put("size",generationImageParam.getSize());
        if(generationImageParam.getStyle() != null) request.put("style",generationImageParam.getStyle());
        if(generationImageParam.getUser() != null) request.put("user",generationImageParam.getUser());


        log.info("构造的请求JSON => {}", JSON.toJSONString(request));
        return JSON.toJSONString(request);
    }

}
