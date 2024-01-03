package cn.haniel.netty.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author hanping
 * @date 2023-12-25
 */
@Slf4j
public class SelectorServer1 {
    public static void main(String[] args) throws IOException {
        // 1. 创建一个 Selector，用于管理多个 channel
        final Selector selector = Selector.open();

        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 2. 注册 selector 到服务器实例上
        // selectionKey 就是将来事件发生后，通过它可以知道事件和哪个 channel 关联
        final SelectionKey selectionKey = ssc.register(selector, 0, null);
        // 设置 selectionKey 只关注 accept 事件
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        log.info("register key: {}", selectionKey);

        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. Selector.select 方法在没有事件时阻塞，防止空轮询，在有事件时恢复运行
            selector.select();

            // 4. 处理事件
            final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                // 这里获得的 selectionKey 和前面注册的是相同的实例，不管连接多少个客户端，这里都是相同实例对象的 selectionKey
                final SelectionKey key = iter.next();
                log.info("key: {}", key);
                // 这里获得的 ssc 和前面的是相同的实例
                final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                final SocketChannel sc = serverSocketChannel.accept();
                log.info("sc: {}", sc);
            }
        }
    }
}
