package com.alandevise.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Filename: Selector.java
 * @Package: com.alandevise.c4
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月17日 16:41
 */

@Slf4j
public class ServerWithSelector {
    // 忽略可能存在的无限循环
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {

        // 1. 创建Selector，管理多个channel
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 2. 建立selector和Channel 之间的联系（注册），将channel注册到selector中去，使其被管理
        // SelectionKey: 事件发生后，通过SelectionKey可知道是什么事件，以及哪一个channel发生的事件
        //              事件类型：
        //              1. accept   是ServerSocket中的事件，有连接请求来了
        //              2. connect  连接一旦建立，便会触发connect事件
        //              3. read     有数据来了，触发read事件，可读事件
        //              4. write    可写事件
        SelectionKey sscKey = serverSocketChannel.register(selector, 0, null);

        // SelectionKey只关注accept事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key: {}", sscKey);

        serverSocketChannel.bind(new InetSocketAddress(8080));

        while (true) {
            // 3. select 方法，没有事件发生，线程阻塞；有事件发生，线程恢复
            selector.select();

            // 4. 处理事件，selectedKeys包含所有发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("key: {}", key);
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                // 建立连接
                SocketChannel ssc = channel.accept();
                log.debug("ServerSocketChannel is {}", ssc);
            }
        }
    }
}
