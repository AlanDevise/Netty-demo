package com.alandevise.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.alandevise.c1.ByteBufferUtil.debugAll;

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

        // 设置SelectionKey只关注accept事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key: {}", sscKey);

        serverSocketChannel.bind(new InetSocketAddress(8080));

        while (true) {

            // 3. select 方法，没有事件发生，线程阻塞；有事件发生，线程恢复
            // select工作方式，事件未处理时，不会阻塞。事件发生后，一定要处理或者取消，不能置之不理
            selector.select();

            // 4. 处理事件，selectedKeys包含所有发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                log.debug("key: {}", key);

                // 5. 区分事件类型
                if (key.isAcceptable()) {   // 如果是accept事件

                    // 获取Channel
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();

                    // 建立连接
                    SocketChannel sc = channel.accept();

                    // 设置SocketChannel 设置为非阻塞
                    sc.configureBlocking(false);

                    // 新建Buffer
                    ByteBuffer buffer = ByteBuffer.allocate(16);    // [CRITICAL] attachment

                    // 将SocketChannel 注册到 Selector 中
                    /* 一个SelectionKey 对应一个SocketChannel，每一个SocketChannel拥有自己的buffer
                     *  互不影响
                     * */
                    SelectionKey scKey = sc.register(selector, 0, buffer);


                    // 设置SelectionKey 的关注点为"可读事件"
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("SocketChannel is {}", sc);

                } else if (key.isReadable()) {  // 如果是read事件
                    try {
                        // 获取Channel
                        SocketChannel channel = (SocketChannel) key.channel();

                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        // 读取数据存入Buffer
                        int read = channel.read(buffer);    // 如果是正常断开，read 返回 -1
                        if (read == -1) {
                            log.info("SocketChannel [{}] 客户端主动断开连接", channel);
                            key.cancel();
                        } else {
                            // 以 \n 拆分解析
                            split(buffer);

                            // 如果position位置和limit位置一致，说明buffer满了，buffer需要扩容
                            if (buffer.position() == buffer.limit()){
                                // 将buffer扩容一倍
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity()*2);
                                // buffer切换为读模式
                                buffer.flip();
                                // 新buffer写入原buffer的内容
                                newBuffer.put(buffer);
                                // 使用新buffer替换掉旧buffer
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 连接异常断开，将key从Selector 的 keys 集合中删除掉
                        key.cancel();
                    }
                }
                // 删除已经处理过的SelectionKey
                iterator.remove();
            }
        }
    }
}