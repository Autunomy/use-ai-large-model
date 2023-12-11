package com.hty.entity.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hty
 * @date 2023-12-08 11:43
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Choices {
    private Integer index;
    private Message message;
    private Delta delta;
    private String finishReason;
}
