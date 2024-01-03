package cn.haniel.netty.server.handler;

import cn.haniel.netty.server.session.Session;
import cn.haniel.netty.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端断连处理
 *
 * @author hanping
 * @date 2024-01-01
 */
@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {

    /**
     * 客户端正常调用 close 断连
     *
     * @param ctx ctx
     * @throws Exception ex
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // session 清理
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("{} 断开", ctx.channel());
    }

    /**
     * 客户端因异常断连
     *
     * @param ctx   ctx
     * @param cause 异常原因
     * @throws Exception ex
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // session 清理
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("{} 异常断开，原因：{}", ctx.channel(), cause);
    }
}
