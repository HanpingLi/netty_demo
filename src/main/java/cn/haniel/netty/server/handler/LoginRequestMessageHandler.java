package cn.haniel.netty.server.handler;

import cn.haniel.netty.message.LoginRequestMessage;
import cn.haniel.netty.message.LoginResponseMessage;
import cn.haniel.netty.server.service.UserServiceFactory;
import cn.haniel.netty.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 匿名内部类转静态内部类，简化方法代码
 * 然后静态内部类提取成单独的类
 *
 * @author hanping
 */
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        final String username = msg.getUsername();
        final String password = msg.getPassword();

        // 校验登录
        final boolean login = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage loginResp;
        if (login) {
            // 将用户和 channel 绑定
            SessionFactory.getSession().bind(ctx.channel(), username);
            loginResp = new LoginResponseMessage(true, "login success");
        } else {
            loginResp = new LoginResponseMessage(false, "username or password error");
        }

        // 响应回客户端
        ctx.writeAndFlush(loginResp);
    }
}
