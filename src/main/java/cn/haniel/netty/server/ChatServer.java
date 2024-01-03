package cn.haniel.netty.server;

import cn.haniel.netty.protocol.MessageCodecSharable;
import cn.haniel.netty.protocol.ProtocolFrameDecoder;
import cn.haniel.netty.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天服务端
 *
 * @author hanping
 * @date 2023-12-31
 */
@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        final NioEventLoopGroup boss = new NioEventLoopGroup();
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        // 可以复用的 handler
        final LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        final MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

        try {
            new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ProtocolFrameDecoder())
                                    .addLast(loggingHandler)
                                    .addLast(messageCodecSharable)
                                    // 用于空闲连接的检测，6s 内未读到数据，会触发 READ_IDLE 事件
                                    .addLast(new IdleStateHandler(6, 0, 0))
                                    // 添加双向处理器，负责处理 READER_IDLE 事件
                                    .addLast(new ChannelDuplexHandler() {
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            final IdleStateEvent event = (IdleStateEvent) evt;
                                            // 读空闲超时，断开连接
                                            if (event.state() == IdleState.READER_IDLE) {
                                                ctx.channel().close();
                                            }
                                        }
                                    })
                                    // 只有消息为登录请求时，才走该 handler
                                    // 都继承 SimpleChannelInboundHandler，只响应一种类型信息
                                    // 匿名内部类通过 idea 抽取成 静态内部了，然后再通过 idea 移到包里单独为类
                                    .addLast(new LoginRequestMessageHandler())
                                    // 单聊 handler
                                    .addLast(new ChatRequestMessageHandler())
                                    // 建群 handler
                                    .addLast(new GroupCreateMessageHandler())
                                    // 群聊 handler
                                    .addLast(new GroupChatRequestHandler())
                                    // TODO 加群退群
                                    // 客户端断开，session 清理
                                    .addLast(new QuitHandler());
                        }
                    })
                    .bind(8080)
                    .sync().channel()
                    .closeFuture().sync();
        } catch (Exception ex) {
            log.error("server error: ", ex);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
