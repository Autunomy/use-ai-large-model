package com.hty.config;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hty
 * @date 2023-12-13 12:01
 * @email 1156388927@qq.com
 * @description
 */

@Configuration
public class JTokkitConfig {

    /***
     * 将注册表注入为bean，防止重复创建，消耗时间
     * @return
     */
    @Bean
    public EncodingRegistry createEncodingRegistry(){
        return Encodings.newDefaultEncodingRegistry();
    }
}
