package com.hty.dao.ai;

import com.hty.entity.pojo.OpenaiChatModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author hty
 * @date 2023-12-14 9:53
 * @email 1156388927@qq.com
 * @description
 */

@Mapper
public interface OpenaiChatModelMapper {

    /***
     * 判断模型是否存在
     * @param modelName 模型名称
     * @return
     */
    boolean checkModelExists(@Param("modelName") String modelName);

    /***
     * 根据模型名称查询模型
     * @param modelName
     * @return
     */
    OpenaiChatModel selectModelByName(@Param("modelName") String modelName);

}
