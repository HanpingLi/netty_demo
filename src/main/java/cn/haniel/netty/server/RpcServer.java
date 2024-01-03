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
 * RPC 服务端
 *
 * @author hanping
 * @date 2023-12-31
 */
@Slf4j
public class RpcServer {
    public static void main(String[] args) {
        final NioEventLoopGroup boss = new NioEventLoopGroup();
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        // 可以复用的 handler
        final LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        final MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        final RpcRequestMessageHandler rpcRequestMessageHandler = new RpcRequestMessageHandler();

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
                                    .addLast(rpcRequestMessageHandler);
                        }
                    })
                    .bind(8080)
                    .sync().channel()
                    .closeFuture().sync();
        } catch (Exception ex) {
            log.error("rpc server error: ", ex);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
