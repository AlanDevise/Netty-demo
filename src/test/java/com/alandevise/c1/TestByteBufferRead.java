package com.alandevise.c1;

import java.nio.ByteBuffer;

import static com.alandevise.c1.ByteBufferUtil.debugAll;

/**
 * @Filename: TestByteBufferRead.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月04日 11:37
 */

public class TestByteBufferRead {

    public static void main(String[] args) {
        // 新建一个buffer并分配大小
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        // 向buffer中写入数据
        byteBuffer.put(new byte[]{'a', 'b', 'c', 'd'});
        // 切换读模式
        byteBuffer.flip();

        // rewind从头开始读
        byteBuffer.get(new byte[4]);
        debugAll(byteBuffer);
        byteBuffer.rewind();
        System.out.println(((char)byteBuffer.get()));

        // mark & reset
        // mark 做一个标记，记录position位置，reset 是将position重置到mark位置
        byteBuffer.rewind();
        System.out.println((char) byteBuffer.get());
        System.out.println((char) byteBuffer.get());
        byteBuffer.mark();      // 这里做一个标记
        System.out.println((char) byteBuffer.get());
        System.out.println((char) byteBuffer.get());
        byteBuffer.reset();     // 将position回到mark位置
        System.out.println((char) byteBuffer.get());
        System.out.println((char) byteBuffer.get());
    }

}
