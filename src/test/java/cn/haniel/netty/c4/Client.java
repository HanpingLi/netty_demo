package cn.haniel.netty.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author hanping
 * @date 2023-12-24
 */
public class Client {
    public static void main(String[] args) throws IOException {
        final SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        System.out.println("start==========>");
    }
}
