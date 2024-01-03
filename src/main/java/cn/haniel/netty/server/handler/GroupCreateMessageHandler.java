package cn.haniel.netty.server.handler;

import cn.haniel.netty.message.GroupCreateRequestMessage;
import cn.haniel.netty.message.GroupCreateResponseMessage;
import cn.haniel.netty.server.session.Group;
import cn.haniel.netty.server.session.GroupSession;
import cn.haniel.netty.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

/**
 * 群聊创建消息处理器
 *
 * @author hanping
 * @date 2024-01-01
 */
@ChannelHandler.Sharable
public class GroupCreateMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        // 构建群组，并通过群聊 session 获取群聊成员的 channel
        final GroupSession groupSession = GroupSessionFactory.getGroupSession();
        final Group group = groupSession.createGroup(msg.getGroupName(), msg.getMembers());

        if (Objects.isNull(group)) {
            // 向建群的人发送成功消息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, msg.getGroupName() + "建群成功"));
            // 向群员发送拉群消息
            groupSession.getMembersChannel(msg.getGroupName())
                    .forEach(channel -> channel.writeAndFlush(new GroupCreateResponseMessage(true, "你已被拉群-" + msg.getGroupName())));
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, "群名重复，建群失败"));
        }
    }
}
