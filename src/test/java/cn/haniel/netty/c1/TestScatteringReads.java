package cn.haniel.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static cn.haniel.netty.util.ByteBufferUtil.debugAll;

/**
 * @author hanping
 * @date 2023-12-24
 */
@Slf4j
public class TestScatteringReads {
    public static void main(String[] args) {
        try (FileChannel channel = new RandomAccessFile("words.txt", "r").getChannel()) {
            // 通过 channel， 将文件内容分散读取到不同的 ByteBuffer 中
            final ByteBuffer b1 = ByteBuffer.allocate(3);
            final ByteBuffer b2 = ByteBuffer.allocate(3);
            final ByteBuffer b3 = ByteBuffer.allocate(5);
            channel.read(new ByteBuffer[]{b1, b2, b3});
            // 切换读模式
            b1.flip();
            b2.flip();
            b3.flip();
            debugAll(b1);
            debugAll(b2);
            debugAll(b3);
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
