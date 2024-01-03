package cn.haniel.netty.server.handler;

import cn.haniel.netty.message.Message;
import cn.haniel.netty.message.RpcRequestMessage;
import cn.haniel.netty.message.RpcResponseMessage;
import cn.haniel.netty.server.service.HelloService;
import cn.haniel.netty.server.service.ServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RPC 请求信息处理
 *
 * @author hanping
 * @date 2024-01-03
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) {

        final RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        response.setMessageType(Message.RPC_MESSAGE_TYPE_RESPONSE);

        // 反射调用服务端的方法
        try {
            final HelloService service = (HelloService) ServiceFactory.getService(Class.forName(msg.getInterfaceName()));
            final Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            final Object result = method.invoke(service, msg.getParameterValue());
            response.setReturnValue(result);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("", e);
            response.setExceptionValue(e.getCause().getMessage());
        }

        ctx.writeAndFlush(response).addListener(promise -> {
            if (!promise.isSuccess()) {
                log.error("", promise.cause());
            }
        });
    }
}
