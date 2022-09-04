package com.alandevise.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Filename: test.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月03日 11:21
 */

@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        // FileChannel 代表数据读写通道
        // 1. 输入输出流可以间接获取 2. RandomAccessFile
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // [1] 准备一个缓冲区
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            while (true) {
                // [2] 从channel 读取数据，意味着向buffer写入数据
                int read = channel.read(byteBuffer);
                // 判断文件是否已经读完，read方法返回值如果是-1，表示已经读到文件末尾
                if (read == -1) {
                    break;
                }
                // [3] 使用数据，打印buffer 内容
                byteBuffer.flip();  // 切换到读模式，默认是写模式。读模式可以读取其中数据。
                while (byteBuffer.hasRemaining()) { // 一直循环到buffer中的最后一个字节
                    byte b = byteBuffer.get();// 无参get方法，表示读取一个字节
                    System.out.println((char) b);
                }
                // [4] 切换为写模式
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
