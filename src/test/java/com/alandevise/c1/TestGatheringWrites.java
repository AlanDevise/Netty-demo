package com.alandevise.c1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @Filename: TestGatheringWrites.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月04日 12:41
 */

public class TestGatheringWrites {
    ByteBuffer b1 = StandardCharsets.UTF_8.encode("hello");
    ByteBuffer b2 = StandardCharsets.UTF_8.encode("world");
    // 汉字在utf8中，占3个字节，所以"你好"占3x2=6 个字节
    ByteBuffer b3 = StandardCharsets.UTF_8.encode("你好");

//    try (FileChannel channel = new RandomAccessFile("data.txt", "rw").getChannel()){
//        channel.write(new ByteBuffer[] {b1, b2, b3});
//    }catch(IOException exception){
//        exception.printStackTrace();
//    }
}
