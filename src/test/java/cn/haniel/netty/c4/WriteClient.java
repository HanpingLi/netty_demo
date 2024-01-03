package cn.haniel.netty.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author hanping
 * @date 2023-12-26
 */
@Slf4j
public class WriteClient {

    public static void main(String[] args) throws IOException {
        final SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));

        int cnt = 0;
        while (true) {
            final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            cnt += sc.read(buffer);
            log.info("已读取：{}", cnt);
            buffer.clear();
        }
    }
}
