package com.hty.service.impl;

import com.alibaba.fastjson.JSON;
import com.hty.constant.Model;
import com.hty.eneity.pojo.GenerationImageParam;
import com.hty.eneity.pojo.ImageResponseBody;
import com.hty.service.ImageService;
import com.hty.utils.Base64ImageUtils;
import com.hty.utils.ImageUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author hty
 * @date 2023-12-08 15:10
 * @email 1156388927@qq.com
 * @description
 */

@Service
public class ImageServiceImpl implements ImageService {

    @Resource
    private ImageUtil imageUtil;
    @Resource
    private Base64ImageUtils base64ImageUtils;

    @Override
    public void generationImage(String prompt) {
        GenerationImageParam generationImageParam = new GenerationImageParam();
        generationImageParam.setPrompt(prompt);
        generationImageParam.setModel(Model.DALL_E_2);
        generationImageParam.setResponseFormat("b64_json");
        String generationImageJSON = imageUtil.generationImage(generationImageParam);

        //获取到了图片列表
        ImageResponseBody.Pair[] imageList = JSON.parseObject(generationImageJSON, ImageResponseBody.class).getData();
        //保存图片列表
        if(generationImageParam.getResponseFormat().equals("b64_json")){
            for (ImageResponseBody.Pair img : imageList) {
                base64ImageUtils.GenerateImage(img.b64_json,"E:\\test.jpg");
            }
        }
    }
}
