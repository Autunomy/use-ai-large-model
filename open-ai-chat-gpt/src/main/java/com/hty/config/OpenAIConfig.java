package com.hty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author hty
 * @date 2023-12-08 15:33
 * @email 1156388927@qq.com
 * @description
 */

@Configuration
public class OpenAIConfig {
    @Value("${openai.apikey}")
    public String apiKey;
}
