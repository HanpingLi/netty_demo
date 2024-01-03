package cn.haniel.netty.client;

import cn.haniel.netty.client.handler.RpcResponseMessageHandler;
import cn.haniel.netty.message.RpcRequestMessage;
import cn.haniel.netty.protocol.MessageCodecSharable;
import cn.haniel.netty.protocol.ProtocolFrameDecoder;
import cn.haniel.netty.protocol.SequenceIdGenerator;
import cn.haniel.netty.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 获取 RPC 客户端 channel 单例对象
 *
 * @author hanping
 * @date 2024-01-02
 */
@Slf4j
public class RpcClientManager {

    public static void main(String[] args) {
        final HelloService service = getProxyService(HelloService.class);
        final String res1 = service.sayHello("zhangsan");
        final String res2 = service.sayHello("lisi");
        log.info("{}", res1);
        log.info("{}", res2);
    }

    /**
     * 客户端调用 HelloService 类的方法时，实际上是调用的 channel 发送消息
     * 因此调用方法时，返回代理后的 HelloService 实例
     *
     * 注意：调用方法的是主线程，但接收服务端消息的是 NIO 线程，因此传递结果就是两个线程之间的通信，用 promise
     */
    public static <T> T getProxyService(Class<T> serviceClass) {
        final ClassLoader loader = serviceClass.getClassLoader();
        final Class[] interfaces = {serviceClass};
        final int sequenceId = SequenceIdGenerator.nextId();
        final Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转成消息对象
            final RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );
            // 2. 发送消息，异步的，交给了 EventLoop 里的 NIO 线程发送
            getChannel().writeAndFlush(msg);

            // 3. 准备一个空的 Promise 对象，准备接收结果                唤醒 promise 去接收结果的线程
            final DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISE_MAP.put(sequenceId, promise);

            // 4. 阻塞，等待 promise 中有结果
            promise.await();

            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                throw new RuntimeException(promise.cause());
            }

        });
        return ((T) o);
    }

    public static final Object LOCK = new Object();

    private static Channel channel = null;

    public static Channel getChannel() {
        if (Objects.nonNull(channel)) {
            return channel;
        }

        synchronized (LOCK) {
            if (Objects.nonNull(channel)) {
                return channel;
            }
            initChannel();
            return channel;
        }

    }

    /**
     * 初始化 channel
     */
    private static void initChannel() {

        final NioEventLoopGroup group = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ProtocolFrameDecoder())
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new MessageCodecSharable())
                                .addLast(new RpcResponseMessageHandler());
                    }
                });

        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            // 这里关闭后操作要异步，不能同步阻塞
            channel.closeFuture().addListener(future -> group.shutdownGracefully());
        } catch (InterruptedException interruptedException) {
            log.error("", interruptedException);
        }
    }
}
