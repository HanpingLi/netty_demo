package cn.haniel.netty.c5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author hanping
 * @date 2023-12-29
 */
@Slf4j
public class TestEchoServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            /**
                             * 读取客户端发送过来的消息
                             */
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                final ByteBuf buf = (ByteBuf) msg;
                                log.info("{}", buf.toString(StandardCharsets.UTF_8));

                                // 写回
                                // 建议使用 ctx.alloc() 创建 ByteBuf
                                final ByteBuf buf2 = ctx.alloc().buffer();
                                buf2.writeBytes(buf);
                                ctx.writeAndFlush(buf2);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}