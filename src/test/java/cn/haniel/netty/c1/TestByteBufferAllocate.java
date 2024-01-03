package cn.haniel.netty.c1;

import java.nio.ByteBuffer;

/**
 * @author hanping
 * @date 2023-12-24
 */
public class TestByteBufferAllocate {
    public static void main(String[] args) {
        System.out.println(ByteBuffer.allocate(10).getClass());
        System.out.println(ByteBuffer.allocateDirect(10).getClass());
    }
}
