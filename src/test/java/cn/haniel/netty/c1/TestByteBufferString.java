package cn.haniel.netty.c1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static cn.haniel.netty.util.ByteBufferUtil.debugAll;

/**
 * @author hanping
 * @date 2023-12-24
 */
public class TestByteBufferString {
    public static void main(String[] args) {

        //================== 将字符串转为 ByteBuffer ==================//
        // 1. 字符串先转字节数组，然后传入 ByteBuffer
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        final byte[] bytes = "HelloWorld".getBytes();
        buffer.put(bytes);
        debugAll(buffer);

        // 2. 直接通过字符串的字节数组构建 ByteBuffer，此时 capacity 就是数组长度
        // 构建完后就是读模式
        final ByteBuffer bf2 = ByteBuffer.wrap("HelloWorld".getBytes());
        debugAll(bf2);

        // 3. 基于字符集工具类的 encode 直接构建 ByteBuffer，注意，此时 ByteBuffer 的 capacity 就是字符长度
        // 构建完后就是读模式
        final ByteBuffer bf3 = StandardCharsets.UTF_8.encode("HelloWorld");
        debugAll(bf3);

        //================== 将 ByteBuffer 中内容转为字符串 ==================//
        // 通过字符集工具的 decode
        final String str = StandardCharsets.UTF_8.decode(bf3).toString();
        System.out.println(str);
        // 注意，ByteBuffer 需要是读模式，position 才会在正确的位置，才能读出内容
        buffer.flip();
        System.out.println(StandardCharsets.UTF_8.decode(buffer));
    }
}
