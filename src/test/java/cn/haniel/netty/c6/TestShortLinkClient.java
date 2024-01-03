package cn.haniel.netty.c6;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hanping
 * @date 2023-12-29
 */
@Slf4j
public class TestShortLinkClient {

    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
            sendMsg();
        }
    }

    private static void sendMsg() {
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                /**
                                 * 向服务端连接
                                 */
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("=============== {} connected ===============", ctx.channel());

                                    log.info("=============== send 1 message ===============");
                                    final ByteBuf buffer = ctx.alloc().buffer();
                                    buffer.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                                    ctx.writeAndFlush(buffer);

                                    // 发完即关
                                    ctx.close();
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
