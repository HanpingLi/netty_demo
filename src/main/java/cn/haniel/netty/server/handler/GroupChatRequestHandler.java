package cn.haniel.netty.server.handler;

import cn.haniel.netty.message.GroupChatRequestMessage;
import cn.haniel.netty.message.GroupChatResponseMessage;
import cn.haniel.netty.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 群聊消息发送handler
 *
 * @author hanping
 * @date 2024-01-01
 */
@ChannelHandler.Sharable
public class GroupChatRequestHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName())
                .forEach(channel -> channel.writeAndFlush(new GroupChatResponseMessage(msg.getFrom(), msg.getContent())));
    }
}
