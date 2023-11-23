package com.hty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
public class BaiduWenXinYiYanApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaiduWenXinYiYanApplication.class, args);
    }

}
