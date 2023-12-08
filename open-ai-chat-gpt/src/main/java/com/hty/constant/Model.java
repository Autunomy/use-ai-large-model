package com.hty.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hty
 * @date 2023-12-07 13:05
 * @email 1156388927@qq.com
 * @description 各种模型的名称
 */

public class Model {
    public static final String GPT_3_5_TURBO = "gpt-3.5-turbo";

    public static final String DALL_E_2 = "dall-e-2";
    public static final String DALL_E_3 = "dall-e-3";

    //图片模型与尺寸的对应关系
    public static final Map<String,String[]> model2ImageSize = new HashMap<>();

    static {
        //初始化图片尺寸
        String[] d2 = new String[]{"256x256","512x512","1024x1024"};
        String[] d3 = new String[]{"1024x1792","1792x1024","1024x1024"};
        model2ImageSize.put("dall-e-2",d2);
        model2ImageSize.put("dall-e-3",d3);
    }
}
