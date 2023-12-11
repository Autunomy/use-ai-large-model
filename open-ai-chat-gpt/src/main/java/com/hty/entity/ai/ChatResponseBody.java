package com.hty.entity.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hty
 * @date 2023-12-08 11:22
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseBody {
    private String id;
    private String object;
    private Date created;
    private Choices[] choices;
    private Usage usage;
}
