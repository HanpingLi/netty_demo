package cn.haniel.netty.c5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author hanping
 * @date 2023-12-28
 */
@Slf4j
public class HandlerServer {

    @Data
    @AllArgsConstructor
    static class Student {
        private String name;
    }

    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 在socketChannel的pipeline中添加handler
                        // pipeline中handler是带有head与tail节点的双向链表，的实际结构为
                        // head <-> handler1 <-> ... <-> handler6 <->tail
                        // Inbound 主要处理入站操作，一般为读操作，发生入站操作时会触发Inbound方法，因此只需要重写 read 方法
                        // Outbound 主要处理出站操作，一般为写操作，发生出站操作时会触发Onbound方法，因此只需要重写 write 方法
                        // 入站时，handler 是从 head 向后调，直到非 Inbound
                        // 出战时，handler 则是从 tail 向前调，直到非 Outbound
                        ch.pipeline()
                                .addLast("handler1", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("handler1: {}", "开始处理接收的信息，解码为字符串");
                                        final ByteBuf buf = (ByteBuf) msg;
                                        final String name = buf.toString(StandardCharsets.UTF_8);
                                        // 父类该方法内部会调用fireChannelRead，将数据传递给下一个handler
                                        super.channelRead(ctx, name);
                                    }
                                })
                                .addLast("handler2", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("handler2: {}", "字符串构建对象");
                                        final Student student = new Student(((String) msg));
                                        super.channelRead(ctx, student);
                                    }
                                })
                                .addLast("handler3", new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("handler3: {}", "打印对象");
                                        log.info("result: {}, class: {}", msg, msg.getClass());

                                        // 触发 write 事件，向客户端发送内容
                                        ch.writeAndFlush(ctx.alloc().buffer().writeBytes("server send msg".getBytes(StandardCharsets.UTF_8)));
                                    }
                                })
                                .addLast("handler4", new ChannelOutboundHandlerAdapter(){
                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.info("handler4");
                                        super.write(ctx, msg, promise);
                                    }
                                })
                                .addLast("handler5", new ChannelOutboundHandlerAdapter(){
                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.info("handler5");
                                        super.write(ctx, msg, promise);
                                    }
                                })
                                .addLast("handler6", new ChannelOutboundHandlerAdapter(){
                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.info("handler6");
                                        super.write(ctx, msg, promise);
                                    }
                                });
                    }
                })
                .bind(8080);
    }
}
