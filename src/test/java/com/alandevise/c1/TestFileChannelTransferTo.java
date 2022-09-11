package com.alandevise.c1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @Filename: TestFileChannelTransferTo.java
 * @Package: com.alandevise.c1
 * @Version: V1.0.0
 * @Description: 1. 将data.txt 中的内容复制至to.txt 中
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月11日 13:33
 */

public class TestFileChannelTransferTo {
    public static void main(String[] args) {
        try (FileChannel from = new FileInputStream("data.txt").getChannel();
             FileChannel to = new FileOutputStream("to.txt").getChannel()) {
            // 效率高，底层会利用操作系统的零拷贝进行优化，最大2G大小，超出大小被忽略
            long size = from.size();
            for (long left = size; left > 0; ) {
                System.out.println("Position: "+(size-left)+" left: "+left);
                left -= from.transferTo((size - left), left, to);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
