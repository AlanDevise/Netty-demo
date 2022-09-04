package com.alandevise.c1;

import java.nio.ByteBuffer;

/**
 * @Filename: TestByteBufferAllocate.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月03日 13:26
 */

public class TestByteBufferAllocate {
    public static void main(String[] args) {
        // HeapByteBuffer - Java堆内存， 读写效率较低，会受到垃圾回收GC的影响
        Class<? extends ByteBuffer> aClass = ByteBuffer.allocate(16).getClass();
        System.out.println(aClass);

        // DirectByteBuffer - 直接内存， 读写效率高（少一次拷贝过程），不受到垃圾回收的影响，分配的效率较低，
        Class<? extends ByteBuffer> aClass1 = ByteBuffer.allocateDirect(16).getClass();
        System.out.println(aClass1);
    }
}
