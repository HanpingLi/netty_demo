package cn.haniel.netty.c7;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * Http协议测试服务器
 *
 * @author hanping
 * @date 2023-12-30
 */
@Slf4j
public class TestHttpServer {

    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new HttpServerCodec())
                                .addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                                        // 获取请求
                                        log.info("uri: {}", msg.uri());

                                        //  构建响应
                                        final DefaultFullHttpResponse resp = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);

                                        final byte[] bytes = "<h1>Hello, World</h1>".getBytes(StandardCharsets.UTF_8);

                                        resp.headers().setInt(CONTENT_LENGTH, bytes.length);
                                        resp.content().writeBytes(bytes);

                                        // 写回响应
                                        ctx.writeAndFlush(resp);
                                    }
                                });
                    }
                })
                .bind(8080);
    }
}
