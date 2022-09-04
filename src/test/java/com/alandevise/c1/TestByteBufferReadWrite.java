package com.alandevise.c1;

import java.nio.ByteBuffer;

import static com.alandevise.c1.ByteBufferUtil.debugAll;

/**
 * @Filename: TestByteBufferReadWrite.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月03日 12:10
 */

public class TestByteBufferReadWrite {
    public static void main(String[] args) {
        // 获取一个bytebuffer，缓存分配10个字节
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        byteBuffer.put((byte) 0x61); // 'a'
        debugAll(byteBuffer);
        byteBuffer.put(new byte[] {0x62,0x63,0x64});
        debugAll(byteBuffer);
        byteBuffer.flip();
        System.out.println(byteBuffer.get());
        debugAll(byteBuffer);
        byteBuffer.compact();
        debugAll(byteBuffer);
    }
}
