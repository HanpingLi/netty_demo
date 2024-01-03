package cn.haniel.netty.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static cn.haniel.netty.util.ByteBufferUtil.debugRead;

/**
 * 阻塞服务端
 *
 * @author hanping
 * @date 2023-12-24
 */
@Slf4j
public class BlockServer {

    // 基于 NIO 的阻塞模式，单线程
    public static void main(String[] args) throws IOException {
        // ByteBuffer，socketChannel 读取的内容需要写入到 buffer
        final ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 构建服务器实例
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        // 2. 服务实例绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 构建连接实例集合
        final List<SocketChannel> channelList = new ArrayList<>();
        while (true) {
            log.info("======== 开始接收通信请求 =========");
            // 4. SocketChannel 用于与客户端间通信（传递信息）
            // ssc 通过 accept 方法接收客户端通信请求，如果 ssc 与客户端成功建立通信，则会生成一个 SocketChannel 实例
            // 注意：当前 accept 方法是阻塞的，线程运行到这如果没有客户端发起通信请求，则线程阻塞
            final SocketChannel sc = ssc.accept();
            log.info("======== 接收通信请求成功，建立 SocketChannel: {} =========", sc);
            channelList.add(sc);

            for (SocketChannel channel : channelList) {
                log.info("======== 开始接收内容, sc: {} =========", sc);
                // 注意：当前 read 方法是阻塞的，线程运行到这如果当前的 channel 没有发送内容，则线程阻塞
                channel.read(buffer);

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
