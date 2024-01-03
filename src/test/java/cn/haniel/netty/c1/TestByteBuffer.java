package cn.haniel.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author hanping
 * @date 2023-12-24
 */
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {

        // 从文件字节输入流从获取 FileChannel
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {

            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                // 从 channel 读取数据，写入 buffer，直到缓冲区满
                final int len = channel.read(buffer);
                log.info("读取到的字节数：{}", len);
                // 读不到数据则退出
                if (len == -1) {
                    break;
                }

                // 切换为写模式
                buffer.flip();
                while (buffer.hasRemaining()) {
                    log.info("{}", (char) buffer.get());
                }

                // 缓冲区清空，切换为写模式
                buffer.clear();

            }
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
