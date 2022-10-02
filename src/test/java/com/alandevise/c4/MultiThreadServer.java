package com.alandevise.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alandevise.c1.ByteBufferUtil.debugAll;

/**
 * @Filename: MultiThreadServer.java
 * @Package: com.alandevise.c4
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022年09月25日 12:07
 */

@Slf4j
public class MultiThreadServer {

    // 忽略可能存在的无限循环
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
        // 开一个名为boss的线程
        Thread.currentThread().setName("boss");
        // 打开服务器SocketChannel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // ServerSocketChannel设置为非阻塞模式
        ssc.configureBlocking(false);
        // 新建选择器
        Selector boss = Selector.open();
        // 将ServerSocketChannel注册到选择器
        SelectionKey bossKey = ssc.register(boss, 0, null);
        // 设置SelectionKey的关注类型为"连接事件"
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        // 将ServerSocketChannel绑定至8080端口
        ssc.bind(new InetSocketAddress(8080));

        // [NOTE] 创建固定数量的worker，这里根据本地机器的CPU核心数来指定worker的数量
        // [WARNING] 如果在Docker环境下，availableProcessors方法只能获取物理机器的CPU核心数，而不是容器配置的核心数
        //           该问题直到JDK10才修复，使用JVM参数 UserContainerSupport配置，默认开启
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];

        // 遍历创建的workers
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }

        // 新建一个计数器用来分配连接到哪一个worker
        AtomicInteger index = new AtomicInteger();

        // 配置完毕，进入无限循环，不停监听事件
        while (true) {
            // 选择器开始监听
            boss.select();
            // 遍历监听到的事件
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            // 如果还有没遍历到的事件，继续遍历
            while (iter.hasNext()) {
                // 获取SelectionKey
                SelectionKey key = iter.next();
                // 将SelectionKey移除，避免重复使用
                iter.remove();
                // 如果是"连接请求"事件
                if (key.isAcceptable()) {
                    // ServerSocketChannel同意连接，并且建立SocketChannel连接
                    SocketChannel sc = ssc.accept();
                    // 将ServerSocket设置为非阻塞模式
                    sc.configureBlocking(false);
                    log.debug("报告大人！连接中！！{}", sc.getRemoteAddress());
                    log.debug("报告大人！ServerChannel注册到worker之前！！{}", sc.getRemoteAddress());
                    // Round robin 轮询
                    // 初始化线程和selector
                    workers[index.getAndIncrement() % workers.length].register(sc);
                    log.debug("报告大人！ServerChannel注册到worker之之后！！{}", sc.getRemoteAddress());
                }
            }
        }
    }

//    // 忽略可能存在的无限循环
//    @SuppressWarnings("InfiniteLoopStatement")
//    static class Worker implements Runnable {
//        // 每个worker拥有独立的selector
//        private volatile Selector selector;
//        // 每个worker拥有自己的名称标识
//        private final String name;
//        // 用来标记register方法是否执行过
//        private volatile boolean start = false;
//        // 线程间传递数据的通道
////        private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
//        private ConcurrentLinkedQueue<Runnable> queue;
//
//        public Worker(String name) {
//            this.name = name;
//        }
//
//        // 初始化线程和selector
//        public void register(SocketChannel sc) throws IOException {
//            if (!start) {
//                // 可能不止一个worker处理数据，所以各个worker开一个线程
//                Thread thread = new Thread(this, name);
//                thread.start();
//                selector = Selector.open();
//                queue = new ConcurrentLinkedQueue<>();
//                start = true;
//            }
//            // 向队列添加了任务，但任务并没有立刻执行
//            queue.add(() -> {
//                // 关联SocketChannel和worker
//                try {
//                    sc.register(selector, SelectionKey.OP_READ,null);
//                } catch (ClosedChannelException e) {
//                    log.info("阿西吧，大人！发生异常了，您且看：{}", e.getMessage());
//                }
//            });
//            // 唤醒selector
//            selector.wakeup();
//        }
//
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    // 监听有无新事件
//                    selector.select();  // worker-0
//                    // 从队列中取出任务
//                    Runnable task = queue.poll();
//                    // 如果任务不为空
//                    if (task != null) {
//                        task.run(); // 执行：sc.register(selector, SelectionKey.OP_READ, null);
//                    }
//                    // 获取SelectionKey迭代器
//                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
//                    // 遍历SelectionKey集合
//                    while (iter.hasNext()) {
//                        // 获取下一个SelectionKey
//                        SelectionKey key = iter.next();
//                        // 将当前的SelectionKey移除，避免重复获取
//                        iter.remove();
//                        // 因为获取到的是ServerChannel所以只关心读写事件
//                        if (key.isReadable()) { // 如果是"可读事件"
//                            // 通过SelectionKey获取SocketChannel
//                            SocketChannel channel = (SocketChannel) key.channel();
//                            // 新建一个Bytebuffer
//                            ByteBuffer buffer = ByteBuffer.allocate(16);
//                            log.debug("报告大人！有数据正在被读取！！{}", channel.getRemoteAddress());
//                            // 将读到的内容放至buffer
//                            channel.read(buffer);
//                            buffer.flip();
//                            debugAll(buffer);
//                        }
//                    }
//                } catch (IOException e) {
//                    log.info("西吧，大人！异常发生了！你看：{}", e.getMessage());
//                }
//            }
//        }
//    }

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
