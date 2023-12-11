package com.hty.service;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Service
@Slf4j
public class OssService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.customDomain}")
    private String customDomain; // 自定义域名

    /***
     * 上传base64格式的图片到阿里云OSS中
     * @param base64Data
     * @param path 路径，数组中每个元素都是一个文件夹
     * @param fileName 名称,需要带后缀名
     * @return 图片在云存储中的url路径
     */
    public String uploadBase64File(String base64Data, String[] path,String fileName) {
        log.info("向阿里云OSS存储base64格式图片");
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        StringBuilder filePathName = new StringBuilder();
        for (String p : path) {
            filePathName.append(p).append("/");
        }


        filePathName.append(fileName);
        log.info("构建好的路径 => {}",filePathName);

        try {
            // 解码Base64字符串为二进制数据
            byte[] data = Base64.getDecoder().decode(base64Data);

            // 上传文件
            ossClient.putObject(bucketName, filePathName.toString(), new ByteArrayInputStream(data));
        } finally {
            if (ossClient != null) {
                // 关闭OSSClient
                ossClient.shutdown();
            }
        }

        //生成url
        String url = "http://";
        if(customDomain != null && !customDomain.equals("")) url += customDomain;
        else url += bucketName + endpoint;
        url += "/" + filePathName;

        return url;
    }
}
