package cn.haniel.netty.server.handler;

import cn.haniel.netty.message.ChatRequestMessage;
import cn.haniel.netty.message.ChatResponseMessage;
import cn.haniel.netty.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

/**
 * 单聊请求 handler
 *
 * @author hanping
 * @date 2024-01-01
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        // 通过单聊会话 session 获取聊天对象的 channel
        final Channel toChannel = SessionFactory.getSession().getChannel(msg.getTo());

        // 在线，发送聊天信息
        if (Objects.nonNull(toChannel)) {
            toChannel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
        // 不在线，返回聊天发起人失败信息
        else {
            ctx.writeAndFlush(new ChatResponseMessage(false, "对方不在线"));
        }

    }
}
