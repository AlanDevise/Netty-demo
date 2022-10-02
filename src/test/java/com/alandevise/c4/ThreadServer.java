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
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alandevise.c1.ByteBufferUtil.debugAll;

/**
 * @Filename: ThreadServer.java
 * @Package: com.alandevise.c4
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-26 9:26
 */

@Slf4j
public class ThreadServer {

    // 忽略可能存在的无限循环
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        try {
            // 打开服务器SocketChannel
            ServerSocketChannel server = ServerSocketChannel.open();
            // 当前线程为Boss线程
            Thread.currentThread().setName("Boss");
            // 将ServerSocketChannel绑定至8080端口
            server.bind(new InetSocketAddress(8080));
            // 负责轮询Accept事件的Selector
            Selector boss = Selector.open();
            // ServerSocketChannel设置为非阻塞模式
            server.configureBlocking(false);
            // 将ServerSocketChannel注册到选择器
            server.register(boss, SelectionKey.OP_ACCEPT);
            // 创建固定数量的Worker
            Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
            // 用于负载均衡的原子整数
            AtomicInteger robin = new AtomicInteger(0);
            for (int i = 0; i < workers.length; i++) {
                workers[i] = new Worker("worker-" + i);
            }
            while (true) {
                boss.select();
                Set<SelectionKey> selectionKeys = boss.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    // BossSelector负责Accept事件
                    if (key.isAcceptable()) {
                        // 建立连接
                        SocketChannel socket = server.accept();
                        log.info("connected...");
                        socket.configureBlocking(false);
                        // socket注册到Worker的Selector中
                        log.info("before read...");
                        // 负载均衡，轮询分配Worker
                        workers[robin.getAndIncrement() % workers.length].register(socket);
                        log.info("after read...");
                    }
                }
            }
        } catch (IOException e) {
            log.info("大人发生异常了！您且看：{}", e.getMessage());
        }
    }

    // 忽略可能存在的无限循环
    @SuppressWarnings("InfiniteLoopStatement")
    static class Worker implements Runnable {
        // 每个worker拥有独立的selector
        private volatile Selector selector;
        // 每个worker拥有自己的名称标识
        private final String name;
        // 用来标记register方法是否执行过
        private volatile boolean started = false;
        /**
         * 同步队列，用于Boss线程与Worker线程之间的通信
         */
        private ConcurrentLinkedQueue<Runnable> queue;

        public Worker(String name) {
            this.name = name;
        }

        public void register(final SocketChannel socket) throws IOException {
            // 只启动一次
            if (!started) {
                Thread thread = new Thread(this, name);
                selector = Selector.open();
                queue = new ConcurrentLinkedQueue<>();
                thread.start();
                started = true;
            }

            // 向同步队列中添加SocketChannel的注册事件
            // 在Worker线程中执行注册事件
            queue.add(() -> {
                try {
                    socket.register(selector, SelectionKey.OP_READ);
                } catch (IOException e) {
                    log.info("阿西吧，大人！发生异常了，您且看：{}", e.getMessage());
                }
            });
            // 唤醒被阻塞的Selector
            // select类似LockSupport中的park，wakeup的原理类似LockSupport中的unpark
            selector.wakeup();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();
                    // 通过同步队列获得任务并运行
                    Runnable task = queue.poll();
                    if (task != null) {
                        // 获得任务，执行注册操作
                        task.run();
                    }
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        // Worker只负责Read事件
                        if (key.isReadable()) {
                            // 简化处理，省略细节
                            SocketChannel socket = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            log.debug("报告大人！有数据正在被读取！！{}", socket.getRemoteAddress());
                            socket.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    log.info("西吧，大人！异常发生了！你看：{}", e.getMessage());
                }
            }
        }
    }
}