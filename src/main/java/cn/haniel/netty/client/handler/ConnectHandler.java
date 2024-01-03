package cn.haniel.netty.client.handler;

import cn.haniel.netty.message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hanping
 * @date 2024-01-01
 */
@Slf4j
public class ConnectHandler extends ChannelInboundHandlerAdapter {
    private final ExecutorService executorService;
    private final CountDownLatch latch;
    private final AtomicBoolean hasLogin;

    public ConnectHandler(ExecutorService executorService, CountDownLatch latch, AtomicBoolean hasLogin) {
        this.executorService = executorService;
        this.latch = latch;
        this.hasLogin = hasLogin;
    }

    /**
     * 在链接建立后，发送登录请求
     *
     * @param ctx ctx
     * @throws Exception ex
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 构建异步线程，发送登录请求
        executorService.submit(() -> {
            final Scanner scanner = new Scanner(System.in);
            System.out.println("username: ");
            final String username = scanner.nextLine();
            System.out.println("password: ");
            final String password = scanner.nextLine();

            // 发送登录请求
            final LoginRequestMessage loginRequestMessage = new LoginRequestMessage(username, password);
            ctx.writeAndFlush(loginRequestMessage);

            // 阻塞，等待服务端响应的处理结果
            try {
                latch.await();
            } catch (InterruptedException interruptedException) {
                log.error("await error: ", interruptedException);
            }

            // 登录失败
            if (!hasLogin.get()) {
                ctx.channel().close();
                return;
            }
            // 登录成功
            // 打印菜单
            while (true) {
                System.out.println("============ 功能菜单 ============");
                System.out.println("send [username] [content]");
                System.out.println("gsend [group name] [content]");
                System.out.println("gcreate [group name] [m1,m2,m3...]");
                System.out.println("gmembers [group name]");
                System.out.println("gjoin [group name]");
                System.out.println("gquit [group name]");
                System.out.println("quit");
                System.out.println("==================================");

                String command = scanner.nextLine();
                final String[] s = command.split(" ");
                switch (s[0]) {
                    // 发送消息
                    case "send" -> ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                    // 群里 发送消息
                    case "gsend" -> ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                    // 创建群
                    case "gcreate" -> {
                        final Set<String> set = new HashSet(Arrays.asList(s[2].split(",")));
                        set.add(username);
                        ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                    }
                    // 查看群列表
                    case "gmembers" -> ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                    case "gjoin" -> ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                    case "gquit" -> ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                    // 触发 【channel.closeFuture().sync(); 向下运行】
                    case "quit" -> ctx.channel().close();
                    default -> {
                        log.error("输入错误");
                        ctx.channel().close();
                    }
                }
            }
        });
    }

    /**
     * 处理服务器的响应信息
     *
     * @param ctx ctx
     * @param msg LoginResponseMessage
     * @throws Exception ex
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("{}", msg);
        // 处理登录请求后的服务端响应
        if (msg instanceof LoginResponseMessage) {
            final LoginResponseMessage response = (LoginResponseMessage) msg;
            if (response.isSuccess()) {
                hasLogin.set(true);
            }
            // 客户端连接 handler 继续执行
            latch.countDown();
        }
    }
}
