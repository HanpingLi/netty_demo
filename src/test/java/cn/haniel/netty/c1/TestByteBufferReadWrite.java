package cn.haniel.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static cn.haniel.netty.util.ByteBufferUtil.debugAll;

/**
 * @author hanping
 * @date 2023-12-24
 */
@Slf4j
public class TestByteBufferReadWrite {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        // 写单个
        // 注意，此时 limit 为写限制，也即缓冲区的大小 -- 10
        buffer.put((byte) 0x61);
        debugAll(buffer);

        // 批量写
        buffer.put(new byte[]{0x62, 0x63, 0x64});
        debugAll(buffer);

        // 切换读模式
        // 注意，此时 limit 会变成读限制 -- 4，而 position 指向下一个可读位置，此处为 0
        buffer.flip();
        debugAll(buffer);

        // 读单个
        log.info("{}", ((char) buffer.get()));
        debugAll(buffer);

        // 所有未读取内容前挪覆盖已读内容，同时 buffer 切换为写模式，limit 转为写限制，position 指向下一个可写入位置
        // 挪动位置后，末尾位置原内容没必要清除，position 指针在后续内容写入时直接覆盖
        buffer.compact();
        debugAll(buffer);

        // 再写入，验证
        buffer.put(new byte[]{0x65, 0x66});
        debugAll(buffer);
    }
}
