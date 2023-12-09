package com.hty.eneity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author hty
 * @date 2023-12-08 9:39
 * @email 1156388927@qq.com
 * @description 请求需要的参数实体类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestParam {
    //[必须]消息列表
    private LinkedList<Map<String, String>> messages;
    //[必须]模型名称
    private String model;
    /*
        使用什么采样温度，介于 0 和 2 之间。较高的值（如 0.8）将使输出更加随机，而较低的值（如 0.2）将使输出更加集中和确定。我们通常建议改变这个
        或top_p但不是两者。
    */
    private Double temperature;
    /*
        一种替代温度采样的方法，称为核采样，其中模型考虑具有 top_p 概率质量的标记的结果。所以 0.1 意味着只考虑构成前 10% 概率质量的标记。
        我们通常建议改变这个或temperature但不是两者。
    */
    private Double topP;
    //为每个输入消息生成多少个聊天完成选项。
    private Integer n;
    //是否开启流式回答
    private Boolean stream;
    //停止生成的标记，API 将停止生成更多标记的最多 4 个序列。可以是一个或多个字符串，最多四个
    private String[] stop;
    //聊天完成时生成的最大标记数。输入标记和生成标记的总长度受模型上下文长度的限制
    private Integer maxTokens;
    //-2.0 和 2.0 之间的数字。正值会根据到目前为止是否出现在文本中来惩罚新标记，从而增加模型谈论新主题的可能性。
    private Double presencePenalty;
    //-2.0 和 2.0 之间的数字。正值会根据新标记在文本中的现有频率对其进行惩罚，从而降低模型逐字重复同一行的可能性。
    private Double frequencyPenalty;
    /*
        修改指定标记出现在完成中的可能性。接受一个 json 对象，该对象将标记（由 GPT 分词器中的标记ID 指定）映射到从 -100 到 100 的相关偏差值。
        您可以使用此标记生成器工具（适用于 GPT-2 和 GPT-3）将文本转换为标记 ID。从数学上讲，偏差会在采样之前添加到模型生成的对数中。
        确切的效果因模型而异，但 -1 和 1 之间的值应该会减少或增加选择的可能性；像 -100 或 100 这样的值应该导致相关标记的禁止或独占选择。
    */
    private Map logitBias;
    //代表您的最终用户的唯一标识符，可以帮助 OpenAI 监控和检测滥用行为。
    private String user;

}
