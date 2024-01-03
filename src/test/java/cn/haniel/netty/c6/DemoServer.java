package cn.haniel.netty.c6;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 粘包 & 半包现象示例
 *
 * @author hanping
 * @date 2023-12-29
 */
@Slf4j
public class DemoServer {
    public static void main(String[] args) throws InterruptedException {
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup(2);

        try {
            final ChannelFuture channelFuture = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 修改系统接收缓冲区，体现半包现象
                    .option(ChannelOption.SO_RCVBUF, 10)
                    // 修改 Netty 的 ByteBuf
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(36, 36, 36))
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new ChannelInboundHandlerAdapter() {

                                        /**
                                         * 客户端连接
                                         */
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            log.info("connected: {}", ctx.channel());
                                            super.channelActive(ctx);
                                        }

                                        /**
                                         * 客户端断连
                                         */
                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            log.info("disconnected: {}", ctx.channel());
                                            super.channelInactive(ctx);
                                        }
                                    });
                        }
                    })
                    .bind(8080);

            log.info("=============== {} binding ===============", channelFuture);

            channelFuture.sync();
            log.info("=============== {} bound ===============", channelFuture.channel());

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            log.error("", ex);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("=============== server stopped ===============");
        }
    }
}
