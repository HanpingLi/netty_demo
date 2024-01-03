package cn.haniel.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author hanping
 * @date 2023-12-24
 */
@Slf4j
public class TestGatheringWrite {
    public static void main(String[] args) {
        final ByteBuffer bf1 = StandardCharsets.UTF_8.encode("Hello");
        final ByteBuffer bf2 = StandardCharsets.UTF_8.encode("World");
        final ByteBuffer bf3 = StandardCharsets.UTF_8.encode("恭喜");

        // 多个 buffer 集中通过一个 Channel 写入同一个文件
        try (FileChannel channel = new RandomAccessFile("words2.txt", "rw").getChannel()) {
            channel.write(new ByteBuffer[]{bf1, bf2, bf3});
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
