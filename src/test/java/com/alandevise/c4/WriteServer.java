package com.alandevise.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @Filename: WriteServer.java
 * @Package: com.alandevise.c4
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-24 16:43
 */

@Slf4j
public class WriteServer {
    public static void main(String[] args) throws IOException {
        // 新建一个服务器通道
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 将通道设置为非阻塞模式
        ssc.configureBlocking(false);

        // 设置选择器
        Selector selector = Selector.open();

        // 将ServerSocketChannel 注册到 Selector 中
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        // 绑定端口
        ssc.bind(new InetSocketAddress(8080));

        while (true){
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while(iter.hasNext()){
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey sckey = sc.register(selector, 0, null);
                    sckey.interestOps(SelectionKey.OP_READ);
                    // 1.连接建立后，向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 3000000; i++){
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    // 实际写入的字节数
                    int write = sc.write(buffer);
                    log.info("实际写入：{}", write);
                    // 3. 判断是否有剩余内容
                    if (buffer.hasRemaining()) {
                        // 4. 关注可写事件    1   4
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);

                        // 5.把未写完的数据挂到sckey上
                        sckey.attach(buffer);
                    }

                }else if(key.isWritable()){
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();

                    int write = sc.write(buffer);
                    log.info("实际写入：{}", write);

                    // 6. 清理操作
                    if (!buffer.hasRemaining()){
                        key.attach(null);   // 需要清除buffer
                        key.interestOps(key.interestOps()-SelectionKey.OP_WRITE);// 不需要关注可写事件
                    }
                }
            }
        }
    }
}
