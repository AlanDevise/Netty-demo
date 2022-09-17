package com.alandevise.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

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
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        System.out.println("waiting...");
    }
}
