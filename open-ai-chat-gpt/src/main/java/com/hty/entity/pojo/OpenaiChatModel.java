package com.hty.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hty
 * @date 2023-12-11 11:45
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenaiChatModel {
    private Integer id;
    private String name;
    private Integer maxTokens;
    private Integer status;
    private Double inputPrice;
    private Double outputPrice;
}
