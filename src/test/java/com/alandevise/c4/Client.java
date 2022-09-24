package com.alandevise.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * @Filename: Client.java
 * @Package: com.alandevise.c4
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月17日 15:26
 */

public class Client {
    public static void main(String[] args) throws IOException {
        // 建立SocketChannel
        SocketChannel socketChannel = SocketChannel.open();
        // 连接至服务器端
        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        SocketAddress address = socketChannel.getLocalAddress();
//        socketChannel.write(Charset.defaultCharset().encode("hello\nworld\n"));
        socketChannel.write(Charset.defaultCharset().encode("012\n3456789abcdef3333\n"));
        System.in.read();
    }
}
