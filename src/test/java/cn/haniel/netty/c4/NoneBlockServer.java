package cn.haniel.netty.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.haniel.netty.util.ByteBufferUtil.debugRead;

/**
 * 非阻塞服务端
 *
 * @author hanping
 * @date 2023-12-25
 */
@Slf4j
public class NoneBlockServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        // ByteBuffer，socketChannel 读取的内容需要写入到 buffer
        final ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 构建服务器实例
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        // 设置为非阻塞模式，这样 accept 就不会阻塞
        ssc.configureBlocking(false);
        // 2. 服务实例绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 构建连接实例集合
        final List<SocketChannel> channelList = new ArrayList<>();
        while (true) {
            Thread.sleep(1000L);
            // 4. SocketChannel 用于与客户端间通信（传递信息）
            // ssc 通过 accept 方法接收客户端通信请求，如果 ssc 与客户端成功建立通信，则会生成一个 SocketChannel 实例
            final SocketChannel sc = ssc.accept();
            // 如果没有客户端建立通信请求，则返回 null
            if (Objects.nonNull(sc)) {
                log.info("======== 接收通信请求成功，建立 SocketChannel: {} =========", sc);
                // sc 也设置为非阻塞模式，这样 read 就不会阻塞
                sc.configureBlocking(false);
                channelList.add(sc);
            }

            for (SocketChannel channel : channelList) {
                // 如果客户端没有内容发送过来，则 read 不阻塞，返回 0
                final int len = channel.read(buffer);
                if (len > 0) {
                    log.info("======== 已接收内容, sc: {} =========", channel);
                    // 查看读取内容
                    buffer.flip();
                    debugRead(buffer);

                    // buffer 清空
                    buffer.clear();
                    log.info("======== 接收内容完成 =========");
                }
            }
        }
    }
}
