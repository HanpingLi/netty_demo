package cn.haniel.netty.c5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author hanping
 * @date 2023-12-27
 */
@Slf4j
public class EventLoopServer {

    public static void main(String[] args) {
        final DefaultEventLoop defaultGroup = new DefaultEventLoop();
        new ServerBootstrap()
                // boss 和 worker，一个 EventLoop 为 boss，两个 EventLoop 为 worker，分属不同的组
                // boss 负责 ServerSocketChannel 上的 accept 事件，worker 负责 SocketChannel 上的 read、write 事件
                .group(new NioEventLoopGroup(1), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("handler1", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        final ByteBuf buf = (ByteBuf) msg;
                                        log.info(buf.toString(StandardCharsets.UTF_8));
                                        // 把 msg 原封不动传给下一个
                                        ctx.fireChannelRead(msg);
                                    }
                                })
                                .addLast(defaultGroup, "handler2", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        final ByteBuf buf = (ByteBuf) msg;
                                        log.info(buf.toString(StandardCharsets.UTF_8));
                                    }
                                });
                    }
                })
                .bind(8080);
    }
}
