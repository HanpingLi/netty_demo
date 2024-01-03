package cn.haniel.netty.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author hanping
 * @date 2023-12-24
 */
public class Client2 {
    public static void main(String[] args) throws IOException {
        final SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        socketChannel.write(StandardCharsets.UTF_8.encode("1234567890"));
        System.out.println("start==========>");
    }
}
