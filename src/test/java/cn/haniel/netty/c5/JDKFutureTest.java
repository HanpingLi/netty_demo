package cn.haniel.netty.c5;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author hanping
 * @date 2023-12-28
 */
@Slf4j
public class JDKFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 构建线程池
        final ExecutorService service = Executors.newFixedThreadPool(2);

        // 2. 提交任务
        final Future<Integer> future = service.submit(() -> {
            log.info("执行计算");
            Thread.sleep(2000);
            return 50;
        });

        // 3. 阻塞等待异步任务结果
        log.info("等待结果");
        final Integer result = future.get();
        log.info("结果：{}", result);

    }
}
