package com.hty.eneity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hty
 * @date 2023-12-08 11:44
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usage {
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
