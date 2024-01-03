package cn.haniel.netty.c1;

import java.nio.ByteBuffer;

import static cn.haniel.netty.util.ByteBufferUtil.debugAll;

/**
 * @author hanping
 * @date 2023-12-24
 */
public class TestByteBufferExam {
    public static void main(String[] args) {
        final ByteBuffer source = ByteBuffer.allocate(32);
        // 接收到黏包和半包的内容
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        // 堆黏包进行拆分
        split(source);
        // 继续接收，解决半包
        source.put("w are you?\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        // 读模式
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到拆分符，与上一个读取位置组成完整的一句内容
            if (source.get(i) == '\n') {
                final int len = i + 1 - source.position();
                // 完整内容放入新的 ByteBuffer 中
                final ByteBuffer target = ByteBuffer.allocate(len);
                for (int j = 0; j < len; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }

        // 可能存在半包，因此不能 clear
        source.compact();
    }


}
