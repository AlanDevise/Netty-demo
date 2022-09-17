package com.alandevise.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.alandevise.c1.ByteBufferUtil.debugRead;

/**
 * @Filename: Sever.java
 * @Package: com.alandevise.c4
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月17日 13:54
 */

@Slf4j
public class Sever {

    // 忽略可能存在的无限循环
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
        // ***使用nio 来理解阻塞模式，单线程***

        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 使用nio 创建服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 将ServerSocketChannel切换为非阻塞模式，「Caution」会影响accept方法，不会阻塞了
        serverSocketChannel.configureBlocking(false);

        // 2. 绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8080));

        // 为了让服务器能够和多个客户端连接，所以将其放在一个while循环中
        // 3. 建立连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {

            // 4. 建立连接 accept [TCP三次握手]
            // 阻塞意味着线程暂停运行；非阻塞意味着线程继续运行，若没有连接：sc = null
            SocketChannel sc = serverSocketChannel.accept();    // 建立成功后，便可通过SocketChannel进行读写操作

            // 判断连接是否为空，不为空则将SocketChannel放到集合中
            if (sc != null) {
                log.info("Connected...{}", sc);
                // 将SocketChannel设置为非阻塞模式，影响read方法，线程仍会继续运行。如果没有读到数据: return 0
                sc.configureBlocking(false);
                channels.add(sc);   // 添加建立的SocketChannel 到连接集合中
            }

            for (SocketChannel socketChannel : channels) {
                // 5. 接收客户端发送的数据
                // read方法也是阻塞方法，如果客户端没有数据发送过来，便一直等待
                int read = socketChannel.read(buffer);

                // 如果read不为0，表示有数据被读取到
                if (read != 0) {
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.info("After read... {}", socketChannel);
                }
            }
        }
    }
}
