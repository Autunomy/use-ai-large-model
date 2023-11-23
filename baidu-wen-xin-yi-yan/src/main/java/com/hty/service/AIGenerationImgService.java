package com.hty.service;

/**
 * @author hty
 * @date 2023-11-23 21:59
 * @email 1156388927@qq.com
 * @description
 */

public interface AIGenerationImgService {

    /***
     * 生成图片 最终需要将其保存在对象存储云中并生成一个url地址返回给前端
     * @param prompt 提示词(必填) 长度限制为1024字符，建议中文或者英文单词总数量不超过150个
     * @param negativePrompt 反向提示词，即用户希望图片不包含的元素。长度限制为1024字符，建议中文或者英文单词总数量不超过150个
     * @param sizeArrayIndex 生成图片长宽，默认值 1024x1024 传过来的是一个下标，需要从sizeArray中获取
     * @param n 生成图片数量  默认值为1 取值范围为1-4 单次生成的图片较多及请求较频繁可能导致请求超时
     * @return 图片的URL地址
     */
    String generationImg(String prompt,
                         String negativePrompt,
                         Integer sizeArrayIndex,
                         Integer n);
}
