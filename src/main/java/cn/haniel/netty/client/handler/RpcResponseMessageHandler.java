package cn.haniel.netty.client.handler;

import cn.haniel.netty.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析 RPC 调用结果
 *
 * @author hanping
 * @date 2024-01-02
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    /**
     * 存储每轮请求的不同线程之间结果
     *
     *                     sequenceId  promise 对象
     */
    public static final Map<Integer, Promise<Object>> PROMISE_MAP = new ConcurrentHashMap<>();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.info("{}", msg);

        final Promise<Object> promise = PROMISE_MAP.remove(msg.getSequenceId());
        if (Objects.nonNull(promise)) {
            if (Objects.nonNull(msg.getReturnValue())) {
                promise.setSuccess(msg.getReturnValue());
            } else {
                promise.setFailure(new RuntimeException(msg.getExceptionValue()));
            }
        }
    }
}
