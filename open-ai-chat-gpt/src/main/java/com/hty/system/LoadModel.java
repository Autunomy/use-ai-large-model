package com.hty.system;

import com.hty.utils.ai.ChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author hty
 * @date 2023-12-15 16:21
 * @email 1156388927@qq.com
 * @description 将所有模型从数据库中加载到redis中
 */

@Component
@Slf4j
public class LoadModel implements ApplicationRunner {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ChatUtil chatUtil;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        //判断redis中是否存在
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey("chatModelSet"))){
            return;
        }

        log.info("从mysql中加载模型到redis中");
        chatUtil.loadModelFromDatabase2Redis();
    }
}
