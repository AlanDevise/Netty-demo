package com.alandevise.c1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.alandevise.c1.ByteBufferUtil.debugAll;

/**
 * @Filename: TestByteBufferExam.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月04日 13:23
 */

public class TestByteBufferExam {
    public static void main(String[] args) {
        /*
         * 黏包，半包问题处理
         * */
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes(StandardCharsets.UTF_8));
        split(source);
        source.put("w are you?\n".getBytes(StandardCharsets.UTF_8));
        split(source);
    }

    private static void split(ByteBuffer source) {
        // 切换为读模式
        source.flip();

        // 在读模式下遍历完整的source
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整的消息
            if (source.get(i) == '\n') {
                // 获取消息长度
                int length = i + 1 - source.position();
                // 将完整的消息存入新的ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        // 切换为写模式
        source.compact();
    }
}
