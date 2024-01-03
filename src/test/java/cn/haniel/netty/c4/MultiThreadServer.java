package cn.haniel.netty.c4;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.haniel.netty.util.ByteBufferUtil.debugAll;

/**
 * @author hanping
 * @date 2023-12-26
 */
@Slf4j
public class MultiThreadServer {

    public static void main(String[] args) throws IOException {
        // 主线程重命名为 boss，只处理 accept 事件，也即只管客户端连接的事
        Thread.currentThread().setName("boss");

        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        final Selector boss = Selector.open();
        final SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);

        // 创建固定数量的 worker 实例并初始化
        final Worker[] workers = new Worker[2];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }

        final AtomicInteger index = new AtomicInteger();
        while (true) {
            boss.select();
            final Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                final SelectionKey key = iter.next();
                iter.remove();
                // 处理 accept 事件
                if (key.isAcceptable()) {
                    final SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.info("connected: {}", sc.getRemoteAddress());

                    // round robin 轮询执行的 worker
                    Worker worker = workers[index.getAndIncrement() % workers.length];

                    // 每次有新的客户端连接过来，都会执行 register
                    // 但只有第一个客户端过来会初始化（启动 worker 线程和 selector）
                    worker.register(sc);

//                    // 这里要把 sc 关联到 worker 的 selector 上，而不是之前的 boss 上，这里分家了
//                    log.info("before register: {}", sc.getRemoteAddress());
//                    sc.register(worker.selector, SelectionKey.OP_READ, null);
//                    log.info("after register: {}", sc.getRemoteAddress());
                }
            }
        }
    }


    /**
     * worker 类
     */
    static class Worker implements Runnable {

        /**
         * 执行该 worker 的线程
         */
        private Thread thread;

        private Selector selector;

        /**
         * worker 名称
         */
        private String name;

        /**
         * 线程间传递可执行任务
         * 这里用于传递 boss 线程到 worker 线程
         */
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        /**
         * 判断 worker 实例是否已经初始化过了
         * 只有第一个客户端过来时才初始化
         */
        private volatile boolean start = false;

        public Worker(String name) {
            this.name = name;
        }

        /**
         * 初始化执行 worker 的线程和对应 selector
         * @param sc
         */
        public void register(SocketChannel sc) throws IOException {
            if (!start) {
                selector = Selector.open();
                final Thread thread = new Thread(this, this.name);
                thread.start();
                start = true;
            }

            // boss 线程执行 register 方法，但需要把 sc 关联 worker 的 selector 语句放到 worker 线程中执行，这里暂存下
            // 每个客户端新连接都会执行以下代码，用于在 worker 线程中关联 sc 和 worker 的 selector
            queue.add(() -> {
                try {
                    sc.register(this.selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    log.error("", e);
                }
            });
            // 此时 worker 的 selector 还没有关联新的 sc，可能会阻塞（在旧的所有 sc 都没读写事件时）
            // 通过 wakeup 把阻塞的 select 方法略过，下一次执行 select 方法时，新 sc 就已经关联上了，可以正常运行
            this.selector.wakeup();
        }

        /**
         * worker 的线程就是监听处理读写事件
         */
        @Override
        public void run() {
            while (true) {
                try {
                    selector.select(); // 第一次执行到这里时，sc 还没关联，会被一直阻塞，所以需要在 register 方法手动 wakeup 一下
                    final Runnable task = queue.poll();
                    if (Objects.nonNull(task)) {
                        // 执行 sc.register(this.selector, SelectionKey.OP_READ, null);
                        task.run();
                    }

                    final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    if (iter.hasNext()) {
                        final SelectionKey key = iter.next();
                        iter.remove();

                        // 这里只需要处理读写事件
                        if (key.isReadable()) {
                            final ByteBuffer buffer = ByteBuffer.allocate(16);
                            final SocketChannel sc = (SocketChannel) key.channel();
                            log.info("read: {}", sc.getRemoteAddress());
                            sc.read(buffer);
                            // TODO：加上客户端退出处理逻辑
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }
}
