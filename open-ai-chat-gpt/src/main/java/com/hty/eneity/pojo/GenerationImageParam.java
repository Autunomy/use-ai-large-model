package com.hty.eneity.pojo;

import com.hty.constant.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hty
 * @date 2023-12-08 15:12
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerationImageParam {

    //图片描述 dall-e-2模型限制1000个字 dall-e-3模型限制4000字
    private String prompt;
    //选择的模型 默认是dall-e-2
    private String model;
    //生成图片的数量 dall-e-2可以是[1,10]  dall-e-3只能是1
    private Integer n;
    //图片的质量 只有dall-e-3支持这个参数
    private String quality;
    //图片相应的格式 url或b64_json格式
    private String responseFormat;
    //图片的尺寸 dall-e-2支持[256x256,512x512,1024x1024]  dall-e-3支持[1024x1024,1792x1024,1024x1792]
    private String size;
    //图片尺寸对应的下标 在Model类中存储有图片尺寸的数组
    private Integer sizeIdx;
    //图片风格 只有dall-e-3支持这个参数 有vivid和natural两种 vivid倾向于生成超真实和戏剧性的图像 natural生成更自然、不太真实的图像。
    private String style;
    //防止用户恶意刷接口的标识
    private String user;

    /***
     * 给size设置值
     */
    public void updateSizeBySizeIdx(){
        if(sizeIdx != null && sizeIdx >= 0 && sizeIdx < 3){
            size = Model.model2ImageSize.get(model)[sizeIdx];
        }
    }

}
