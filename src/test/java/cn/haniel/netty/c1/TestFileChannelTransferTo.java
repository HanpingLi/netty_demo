package cn.haniel.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author hanping
 * @date 2023-12-24
 */
@Slf4j
public class TestFileChannelTransferTo {

    public static void main(String[] args) {
        try (
                final FileChannel from = new FileInputStream("data.txt").getChannel();
                final FileChannel to = new FileOutputStream("to.txt").getChannel()
        ) {
            // 这种文件复制方式效率很高，底层利用了操作系统零拷贝进行优化
            from.transferTo(0, from.size(), to);
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
