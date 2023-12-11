package com.hty.constant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author hty
 * @date 2023-12-10 12:02
 * @email 1156388927@qq.com
 * @description
 */

public class ImageModel {
    public static final String DALL_E_2 = "dall-e-2";
    public static final String DALL_E_3 = "dall-e-3";
    //模型列表
    public static final Set<String> imageModelSet = new HashSet<>();

    //图片模型与尺寸的对应关系
    public static final Map<String,String[]> model2ImageSize = new HashMap<>();

    static {
        imageModelSet.add(DALL_E_2);
        imageModelSet.add(DALL_E_3);
        //初始化图片尺寸
        String[] d2 = new String[]{"256x256","512x512","1024x1024"};
        String[] d3 = new String[]{"1024x1792","1792x1024","1024x1024"};
        model2ImageSize.put("dall-e-2",d2);
        model2ImageSize.put("dall-e-3",d3);
    }
}
