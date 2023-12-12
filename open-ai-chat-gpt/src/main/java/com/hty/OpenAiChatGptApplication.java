package com.hty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class OpenAiChatGptApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenAiChatGptApplication.class, args);
    }

}
