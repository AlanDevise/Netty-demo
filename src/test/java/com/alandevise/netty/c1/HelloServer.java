package com.alandevise.netty.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;

/**
 * @Filename: HelloServer.java
 * @Package: com.alandevise.netty.c1
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-10-02 11:46
 */

public class HelloServer {
    public static void main(String[] args) {
        // 1. 服务器端启动器，负责组装netty组件，协调工作，使其启动
        new ServerBootstrap()
                // 2. 添加组件 NioEventLoopGroup：<1> 包含selector监听事件类型   <2> 包含Thread
                .group(new NioEventLoopGroup())
                // 3. 选择一种ServerSocketChannel 实现方法，NIO，OIO，BIO
                .channel(NioServerSocketChannel.class)
                // 4. 决定Handler能够处理哪些操作
                .childHandler(
                        // 5. 初始化一个和客户端的读写通道，负责添加别的handler
                        new ChannelInitializer<NioSocketChannel>() {
                            // 6. 具体添加了哪些handler
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                // 将ByteBuf 转换成 String，StringDecoder解码器
                                ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                                // 自定义handler
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    // 表示处理读事件
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                        // 打印上一步转换好的字符串
                                        System.out.println(msg);
                                    }
                                });
                            }
                        })
                // 7. 绑定监听端口
                .bind(8080);
    }
}
