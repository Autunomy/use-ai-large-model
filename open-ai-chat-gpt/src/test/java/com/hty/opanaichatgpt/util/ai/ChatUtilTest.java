package com.hty.opanaichatgpt.util.ai;

import com.hty.constant.ChatModel;
import com.hty.utils.ai.ChatUtil;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.ModelType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Scanner;

/**
 * @author hty
 * @date 2023-12-13 11:53
 * @email 1156388927@qq.com
 * @description
 */


//@SpringBootTest
public class ChatUtilTest {

    @Test
    public void computeTokenTest(){
        //创建一个EncodingRegistry 这里可以将其注入为一个Bean对象 因为这个对象的创建成本比较高
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        //获取模型对应的编码方式
        Encoding encoding = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);
        //使用获得到的编码方式计算token数量
        Scanner scanner = new Scanner(System.in);
        System.out.println(encoding.countTokens(scanner.next()));
    }
}
