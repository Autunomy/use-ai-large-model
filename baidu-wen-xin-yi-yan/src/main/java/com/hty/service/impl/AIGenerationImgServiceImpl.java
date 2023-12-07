package com.hty.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hty.service.AIGenerationImgService;
import com.hty.utils.Base64ImageUtils;
import com.hty.utils.WenXinImgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author hty
 * @date 2023-11-23 21:59
 * @email 1156388927@qq.com
 * @description
 */

@Slf4j
@Service
public class AIGenerationImgServiceImpl implements AIGenerationImgService {

    @Resource
    private WenXinImgUtils wenXinImgUtils;
    @Resource
    private Base64ImageUtils base64ImageUtils;


    @Override
    public String generationImg(String prompt, String negativePrompt, Integer sizeArrayIndex, Integer n) {
        String responseJson = wenXinImgUtils.generationImg(prompt, negativePrompt, sizeArrayIndex, n);
        JSONObject responseJsonObject = JSON.parseObject(responseJson);
        JSONArray data = JSON.parseArray(responseJsonObject.getString("data"));

        int i = 0;
        while(i < data.size()){
            JSONObject imgJsonObject = JSON.parseObject(data.getString(i));
            //图片的base64格式
            String imgBase64 = imgJsonObject.getString("b64_image");
            base64ImageUtils.GenerateImage(imgBase64,"E:\\test.jpg");
            i++;
        }
        return "success";
    }
}
