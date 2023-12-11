package com.hty.service;

import java.util.List;

/**
 * @author hty
 * @date 2023-12-08 15:09
 * @email 1156388927@qq.com
 * @description
 */


public interface ImageService {

    /***
     * 图片生成
     * @param prompt 提示词
     * @return 返回的是图片url列表
     */
    List<String> generationImage(String prompt);
}
