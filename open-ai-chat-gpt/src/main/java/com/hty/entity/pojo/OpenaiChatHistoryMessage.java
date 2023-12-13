package com.hty.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hty
 * @date 2023-12-11 11:46
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenaiChatHistoryMessage {
    private Integer id;
    private String messageCard;
    private String role;
    private String content;
    private Date createTime;
    private Integer tokens;
    private Integer chatWindowId;
}
