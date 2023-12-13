package com.hty.utils;

import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;

/**
 * @author hty
 * @date 2023-11-23 22:14
 * @email 1156388927@qq.com
 * @description Base64转图片工具类，只能将图片存储在本地
 */

@Component
public class Base64ImageUtils {

    /***
     * 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
     * @param imgFilePath 图片在本机路径地址
     * @return
     */
    public String getImageStr(String imgFilePath) {
        byte[] data = null;

        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imgFilePath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);// 返回Base64编码过的字节数组字符串
    }

    /***
     * 对字节数组字符串进行Base64解码并生成图片 保存在本地
     * @param imgStr 待解码的Base64编码
     * @param imgFilePath 保存文件的路径
     * @return
     */
    public boolean generateImage(String imgStr, String imgFilePath) {
        if (imgStr == null) // 图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] bytes = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
