package cn.haniel.netty.c6;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * @author hanping
 * @date 2023-12-29
 */
@Slf4j
public class TestFixLenClient {

    public static void main(String[] args) {
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        /**
                                         * 向服务端连接
                                         */
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            log.info("=============== {} connected ===============", ctx.channel());

                                            log.info("=============== send 1 message ===============");
                                            final Random random = new Random();
                                            char c = 'a';
                                            final ByteBuf buffer = ctx.alloc().buffer();
                                            for (int i = 0; i < 10; i++) {
                                                byte[] bytes = new byte[10];
                                                for (int j = 0; j <= random.nextInt(10); j++) {
                                                    bytes[j] = ((byte) c);
                                                }
                                                c++;
                                                buffer.writeBytes(bytes);
                                            }
                                            ctx.writeAndFlush(buffer);
                                        }
                                    });
                        }
                    })
                    .connect("localhost", 8080)
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();
        } catch (InterruptedException interruptedException) {
            log.error("", interruptedException);
        } finally {
            workerGroup.shutdownGracefully();
            log.info("=============== client stopped ===============");
        }
    }
}
