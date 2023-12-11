package com.hty.service.impl;

import com.alibaba.fastjson.JSON;
import com.hty.constant.ImageModel;
import com.hty.entity.ai.GenerationImageParam;
import com.hty.entity.ai.ImageResponseBody;
import com.hty.service.ImageService;
import com.hty.service.OssService;
import com.hty.utils.Base64ImageUtils;
import com.hty.utils.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author hty
 * @date 2023-12-08 15:10
 * @email 1156388927@qq.com
 * @description
 */

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    @Resource
    private ImageUtil imageUtil;
    @Resource
    private Base64ImageUtils base64ImageUtils;
    @Resource
    private OssService ossService;

    private static final String[] PATH = new String[]{"openai","image-generation"};

    @Override
    public List<String> generationImage(String prompt) {
        //返回值
        List<String> urlList = new ArrayList<>();

        GenerationImageParam generationImageParam = new GenerationImageParam();
        generationImageParam.setPrompt(prompt);
        generationImageParam.setModel(ImageModel.DALL_E_3);
        generationImageParam.setResponseFormat("b64_json");
        generationImageParam.setSizeIdx(0);
        String generationImageJSON = imageUtil.generationImage(generationImageParam);

        //获取到了图片列表
        ImageResponseBody.Pair[] imageList = JSON.parseObject(generationImageJSON, ImageResponseBody.class).getData();
        //保存图片列表
        if(generationImageParam.getResponseFormat().equals("b64_json")){
            for (ImageResponseBody.Pair img : imageList) {
                //生成一个文件名称
                String fileName = UUID.randomUUID().toString().replace("-","") + ".jpg";
                //保存到本地
//                base64ImageUtils.generateImage(img.b64_json,"E:\\test.jpg");
                //保存到OSS对象存储中
                String url = ossService.uploadBase64File(img.b64_json, PATH, fileName);
                log.info("图片url => {}",url);
                urlList.add(url);
            }
        }
        return urlList;
    }
}
