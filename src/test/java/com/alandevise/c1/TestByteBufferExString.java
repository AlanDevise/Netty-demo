package com.alandevise.c1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.alandevise.c1.ByteBufferUtil.debugAll;

/**
 * @Filename: TestByteBufferExString.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月04日 11:57
 */

public class TestByteBufferExString {
    public static void main(String[] args) {
        /*
        * 1. 将字符串转为ByteBuffer
        * 进行网络传输时，数据信息不是以字符串的形式传输的
        * String 字符串 ---> ByteBuffer ---> String 字符串
        * */
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        String message = "Hello";
        byteBuffer.put(message.getBytes());
        debugAll(byteBuffer);

        /*
        * 2. charset 字符集 [Alan 推荐]
        * */
        ByteBuffer message2 = StandardCharsets.UTF_8.encode("Hello");
        debugAll(message2);

        /*
        * 3. wrap
        * */
        ByteBuffer byteBuffer1 = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        debugAll(byteBuffer1);


        // 将ByteBuffer 转换为String
        byteBuffer.flip();
        String str1 = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        System.out.println(str1);

        String str2 = StandardCharsets.UTF_8.decode(byteBuffer1).toString();
        System.out.println(str2);
    }
}
