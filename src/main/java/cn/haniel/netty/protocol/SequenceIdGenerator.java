package cn.haniel.netty.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hanping
 */
public abstract class SequenceIdGenerator {

    private static final AtomicInteger ID = new AtomicInteger();

    // 递增 和 获取
    public static int nextId(){ return ID.incrementAndGet(); }

}