package com.hty.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hty
 * @date 2023-12-11 11:45
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenaiChatWindow {

    private Integer id;
    private Integer userId;
    private String windowId;
    private Integer modelId;
    private Date createTime;
    private String title;
}
