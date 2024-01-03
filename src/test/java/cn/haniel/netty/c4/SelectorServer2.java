package cn.haniel.netty.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static cn.haniel.netty.util.ByteBufferUtil.debugAll;
import static cn.haniel.netty.util.ByteBufferUtil.debugRead;

/**
 * @author hanping
 * @date 2023-12-25
 */
@Slf4j
public class SelectorServer2 {

    private static void split(ByteBuffer source) {
        // 读模式
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到拆分符，与上一个读取位置组成完整的一句内容
            if (source.get(i) == '\n') {
                final int len = i + 1 - source.position();
                // 完整内容放入新的 ByteBuffer 中
                final ByteBuffer target = ByteBuffer.allocate(len);
                for (int j = 0; j < len; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }

        // 可能存在半包，因此不能 clear
        source.compact();
    }

    public static void main(String[] args) throws IOException {
        // 1. 创建一个 Selector，用于管理多个 channel
        // selector 内部有一个集合，所有注册到 selector 上的 ssc、sc，都会对应生成一个 selectionKey，这个 selectionKey 会被放入这个集合
        // 暂时称为 registerKeys
        final Selector selector = Selector.open();

        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 2. 服务器实例注册到 Selector 上，获得对应的一个 selectionKey
        // selectionKey 就是将来事件发生后，通过它可以知道事件和哪个 channel 关联
        final SelectionKey sscKey = ssc.register(selector, 0, null);
        // 设置 ssc 的 selectionKey 只关注 accept 事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.info("sscKey: {}", sscKey.hashCode());

        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. Selector.select 方法在没有事件时阻塞，防止空轮询，在有事件时恢复运行
            // 当 selector 接收到事件后，会将这个`事件 + 对应的 selectionKeys 实例`放入一个新的集合中，称为 selectedKeys
            // 例如：1. ssc 的 selectionKey 实例已经在 registerKeys 集合中，但当 accept 事件发生时，`ssc 的 selectionKey 实例 + accept 事件`也会被放入 selectedKeys 集合中
            // 2. 当 read 事件发生时，`sc 的 selectionKey 实例 + read 事件` 也会被放入 selectedKeys 集合中
            selector.select();

            // 4. 处理事件，也即把 selectedKeys 集合中的 selectionKey 遍历处理
            // select 在事件未处理时，它不会阻塞，事件发生后，要么处理（accept），要么取消（key.cancel），不能置之不理
            final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                final SelectionKey key = iter.next();
                log.info("key: {}", key);
                // 注意：selector 只会往 selectedKeys 集合加 `selectionKey + 事件`
                // 但当事件处理完成后，事件消失，而对应的 selectionKey 却不会自动从 selectedKeys 集合移除
                // 如果不手动移除，会导致下一次循环处理到没有事件的 selectionKey 而报空指针异常
                iter.remove();

                // 区分事件类型
                // accept 事件，也即 selectionKey 是 ssc 的，客户端连接成功，开始处理 sc，给 sc 注册到 selector 上，获取对应的 selectionKey，关注 read 事件
                if (key.isAcceptable()) {
                    // 这里获得的 ssc 和前面的是相同的实例
                    final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    final SocketChannel sc = serverSocketChannel.accept();
                    sc.configureBlocking(false);
                    // 将 buffer 作为附件关联到 sc 的 selectionKey 上
                    final ByteBuffer buffer = ByteBuffer.allocate(16);
                    final SelectionKey scKey = sc.register(selector, 0, buffer);
                    // sc 的 selectionKey 实例也会被放入 registerKeys 集合中
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.info("sc: {}", sc);
                    log.info("scKey: {}", scKey.hashCode());
                }
                // read 事件，也即 selectionKey 是 sc 的，说明 sc 对应的客户端发送消息了
                else if (key.isReadable()) {
                    try {
                        final SocketChannel sc = (SocketChannel) key.channel();
                        // 获取 scKey 附件的 buffer
                        final ByteBuffer buffer = (ByteBuffer) key.attachment();
                        final int read = sc.read(buffer);
                        // 当客户端正常断开时，也会一直触发对应 scKey 的 read 事件，但不会抛出异常，而是 read 方法返回 -1
                        // 也需要将 scKey 从 registerKeys 集合中去除
                        if (read == -1) {
                            key.cancel();
                        }
                        // 读取内容
                        else {
                            // 切割读取内容
                            split(buffer);
                            // 如果 buffer.compact 后，buffer 内容依旧满，说明没有读取到分隔符，也说明 read 事件还没结束
                            // 扩容后把内容放进新 buffer，将新 buffer 作为 scKey 的附件，再去接收剩余的信息
                            if (buffer.position() == buffer.limit()) {
                                final ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (Exception e) {
                        log.error("", e);
                        // 当客户端异常断连时，会一直触发对应 scKey 的 read 事件，且抛出异常，因此需要抓住异常并将 scKey 从 registerKeys 集合中去除
                        key.cancel();
                    }
                }

            }
        }
    }
}
