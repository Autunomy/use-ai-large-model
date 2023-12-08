package com.hty.eneity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @author hty
 * @date 2023-12-08 15:20
 * @email 1156388927@qq.com
 * @description 图片生成相应的实体类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponseBody {
    public static class Pair{
        public String url;
        public String b64_json;
    }
    private Date created;
    private Pair[] data;

}


