package com.hty.controller.ai;

import com.hty.service.ai.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hty
 * @date 2023-12-08 15:05
 * @email 1156388927@qq.com
 * @description
 */

@RestController
@Slf4j
@RequestMapping("/openai")
public class ImageController {

    @Resource
    private ImageService imageService;


    @PostMapping("/generation/image")
    public List<String> generationImage(String prompt){
        return imageService.generationImage(prompt);
    }

}
