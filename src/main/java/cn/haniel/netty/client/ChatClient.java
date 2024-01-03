package cn.haniel.netty.client;

import cn.haniel.netty.client.handler.ConnectHandler;
import cn.haniel.netty.message.PingMessage;
import cn.haniel.netty.protocol.MessageCodecSharable;
import cn.haniel.netty.protocol.ProtocolFrameDecoder;
import cn.haniel.netty.util.NamesThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 聊天客户端
 *
 * @author hanping
 * @date 2023-12-31
 */
@Slf4j
public class ChatClient {
    public static void main(String[] args) {

        final ExecutorService executorService = new ThreadPoolExecutor(5, 5, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(200),
                new NamesThreadFactory("login-"), new ThreadPoolExecutor.DiscardOldestPolicy());

        final CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean hasLogin = new AtomicBoolean(false);

        final NioEventLoopGroup group = new NioEventLoopGroup();
        // 可以复用的 handler
        final LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        final MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

        try {
            new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ProtocolFrameDecoder())
                                    .addLast(loggingHandler)
                                    .addLast(messageCodecSharable)
                                    // 发送心跳包，让服务器知道客户端在线
                                    // 3s未发生WRITER_IDLE，就像服务器发送心跳包
                                    // 该值为服务器端设置的READER_IDLE触发时间的一半左右
                                    .addLast(new IdleStateHandler(0, 3, 0))
                                    .addLast(new ChannelDuplexHandler() {
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            IdleStateEvent event = (IdleStateEvent) evt;
                                            if (event.state() == IdleState.WRITER_IDLE) {
                                                // 发送心跳包
                                                ctx.writeAndFlush(new PingMessage());
                                            }
                                        }
                                    })
                                    .addLast("client handler", new ConnectHandler(executorService, latch, hasLogin));
                        }
                    })
                    .connect("localhost", 8080)
                    .sync().channel()
                    .closeFuture()
                    .sync();
        } catch (InterruptedException ex) {
            log.error("client error: ", ex);
        } finally {
            group.shutdownGracefully();
        }

    }

}
