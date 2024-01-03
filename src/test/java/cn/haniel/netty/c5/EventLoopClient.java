package cn.haniel.netty.c5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author hanping
 * @date 2023-12-27
 */
@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        final ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));
        log.info("{}", channelFuture.channel());
        channelFuture.addListener((ChannelFutureListener) future -> {
            log.info("thread: {}", Thread.currentThread().getName());
            log.info("{}", channelFuture.channel());
            future.channel().writeAndFlush("hello");
        });
    }
}
