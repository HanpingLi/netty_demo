package cn.haniel.netty.c5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * @author hanping
 * @date 2023-12-27
 */
@Slf4j
public class CloseFutureClient {

    public static void main(String[] args) throws InterruptedException {
        final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        final ChannelFuture channelFuture = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));
        final Channel channel = channelFuture.sync().channel();

        new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);
            while (true) {
                final String lineContent = scanner.nextLine();
                if ("q".equals(lineContent)) {
                    // close 方法异步执行，input 线程调用，nioEventLoopGroup 线程执行
                    channel.close();
                    break;
                }
                channel.writeAndFlush(lineContent);
            }
        }, "input").start();

        // 方法一：使用 channelFuture.closeFuture().sync() 阻塞当前线程（main），直到 EventLoop 内的线程关闭 channel 完成
//        channel.closeFuture().sync();
//        log.info("善后操作");
//      // 关闭 eventLoopGroup
//        eventLoopGroup.shutdownGracefully();

        // 方法二：使用 channel.closeFuture().addListener(回调对象) 异步执行善后操作，不让当前线程（main）去处理，全部交由 EventLoop 内的线程处理（当甩手掌柜）
        channel.closeFuture().addListener((ChannelFutureListener) future -> {
            log.info("善后操作");
            // 关闭 eventLoopGroup
            eventLoopGroup.shutdownGracefully();
        });
    }
}
