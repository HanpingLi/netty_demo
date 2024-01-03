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

/**
 * @author hanping
 * @date 2023-12-26
 */
@Slf4j
public class SelectorWriteServer {

    public static void main(String[] args) throws IOException {
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        final Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);

        while (true) {
            selector.select();
            final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                final SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) {
                    final SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    final SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ); // 订阅 read 事件，以接收客户端发送的消息

                    // 1. 向客户端发送大量数据
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 50000000; i++) {
                        sb.append("a");
                    }
                    final ByteBuffer buffer = StandardCharsets.UTF_8.encode(sb.toString());

                    // 因为写入的内容过长，不会一次性全部写入，而是每次写一段
                    // 返回值代表本次写入多少
                    final int write = sc.write(buffer);
                    log.info("本次写入：{}", write);

                    // 如果上次没写完，则 buffer 内还有剩余内容，会自动触发 sc 的 write 事件
                    // 所以在当前的 accept 事件触发完成后，还需要让 sc 订阅 write 事件，发送剩余内容
                    if (buffer.hasRemaining()) {
                        // 订阅 write 事件的同时，也要把 scKey 之前订阅的事件一并带上
                        // 又因为事件的值都是 2 的 n 次方，直接相加也可以拆分开知道具体是哪个（二进制下算算看） 0001 + 0100 = 0101
                        // 这里用 或 来做，更直接
                        scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);

                        // 同一个 sc 应该只操作相同的 buffer，且没有读完的 buffer 也应该跟着到下一次事件处理
                        // buffer 放到 sc 的附件中
                        scKey.attach(buffer);
                    }
                }
                else if (key.isWritable()) {
                    final ByteBuffer buffer = (ByteBuffer) key.attachment();
                    final SocketChannel sc = (SocketChannel) key.channel();
                    final int write = sc.write(buffer);
                    log.info("本次写入：{}", write);

                    // 如果本次还没写完，则 buffer 会继续触发 write 事件
                    // 如果本次已经写完，则应当将 buffer 清理出附件，同时取消订阅 write 事件
                    if (!buffer.hasRemaining()) {
                        key.attach(null);
                        // 0101 - 0100 = 0001
                        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}
