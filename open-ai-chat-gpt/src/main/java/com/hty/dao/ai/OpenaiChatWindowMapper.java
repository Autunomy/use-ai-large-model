package com.hty.dao.ai;

import com.hty.entity.pojo.OpenaiChatWindow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author hty
 * @date 2023-12-12 8:57
 * @email 1156388927@qq.com
 * @description
 */

@Mapper
public interface OpenaiChatWindowMapper {

    /***
     * 创建一个聊天窗口(上下文)
     * @param window
     */
    Integer createWindow(@Param("window")OpenaiChatWindow window);


    /***
     * 根据windowId获取id
     * @param windowId
     * @return
     */
    Integer getIdByWindowId(@Param("windowId")String windowId);

    /***
     * 判断窗口标题是否被AI生成过
     * @param windowId
     * @return
     */
    Integer getWindowTitleStatus(@Param("windowId")String windowId);

    /***
     * 修改聊天窗口的标题以及状态
     * @param openaiChatWindow
     * @return
     */
    Integer updateWindowTitleAndStatus(@Param("chatWindow") OpenaiChatWindow openaiChatWindow);
}
