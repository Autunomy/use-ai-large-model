package com.hty.constant;

/**
 * @author hty
 * @date 2023-11-23 16:39
 * @email 1156388927@qq.com
 * @description 目前项目中可调用的文心的模型以及对应的url
 */

public enum WenXinModel {

    ERNIE_Bot_4_0("ERNIE-Bot4.0","https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro"),
    ERNIE_Bot("ERNIE_Bot","https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions"),
    ERNIE_Bot_8K("ERNIE-Bot-8K","https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_bot_8k"),
    ERNIE_Bot_turbo("ERNIE-Bot-turbo","https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant")
    ;


    private final String name;
    private final String url;

    private WenXinModel(String name,String url){
        this.name = name;
        this.url = url;
    }

    public static String getUrl(WenXinModel wenXinModel){
        return wenXinModel.url;
    }
}
