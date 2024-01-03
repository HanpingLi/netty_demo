package cn.haniel.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static cn.haniel.netty.util.ByteBufferUtil.debugAll;

/**
 * @author hanping
 * @date 2023-12-24
 */
@Slf4j
public class TestByteBufferRead {

    public static void main(String[] args) {
        final ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});
        buffer.flip();

        // 从头开始读到尾，可看到 position 指针已经指向 4
        buffer.get(new byte[4]);
        debugAll(buffer);

        // 如果想重头再读，则使用 rewind，会重新设置 position 到 0
        buffer.rewind();
        debugAll(buffer);

        // mark & reset
        // mark 标记 position 位置， reset 重置到 mark 标记的 position 位置，用于重复读取某段内容
        log.info("{}", ((char) buffer.get()));
        log.info("{}", ((char) buffer.get()));
        // 标记此时的 position
        buffer.mark();
        log.info("{}", ((char) buffer.get()));
        log.info("{}", ((char) buffer.get()));
        // 重置到之前标记的位置，再读
        buffer.reset();
        log.info("{}", ((char) buffer.get()));
        log.info("{}", ((char) buffer.get()));

        // get(i)，读指定 position 的内容，但不会改变 position
        debugAll(buffer);
        log.info("{}", ((char) buffer.get(3)));
        debugAll(buffer);
    }
}
