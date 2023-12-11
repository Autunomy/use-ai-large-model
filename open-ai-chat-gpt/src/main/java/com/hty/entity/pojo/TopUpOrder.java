package com.hty.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hty
 * @date 2023-12-11 11:49
 * @email 1156388927@qq.com
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopUpOrder {
    private Integer id;
    private Date createTime;
    private Double countDollar;
    private Double countRmb;
    private String orderId;
    private Integer userId;
}
