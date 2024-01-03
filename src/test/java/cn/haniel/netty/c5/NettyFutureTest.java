package cn.haniel.netty.c5;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @author hanping
 * @date 2023-12-28
 */
@Slf4j
public class NettyFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 获取 EventLoop，同线程池
        final NioEventLoopGroup group = new NioEventLoopGroup(2);
        final EventLoop eventLoop = group.next();

        // 2. 提交异步任务
        final Future<Integer> future = eventLoop.submit(() -> {
            log.info("执行计算");
            Thread.sleep(2000);
            return 50;
        });

        // 3. 接收结果
        // 3.1 同步阻塞
//        log.info("等待结果");
//        final Integer result = future.get();
//        log.info("结果：{}", result);
        // 3.2 异步回调
        future.addListener(future1 -> {
            // 这里用 get 或者 getNow 都行，能回调说明已经有结果，即便用 get 也不会阻塞
            final Integer result = (Integer) future1.getNow();
            log.info("结果：{}", result);
        });
    }
}
