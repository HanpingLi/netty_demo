package cn.haniel.netty.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 专门适配自定义协议的帧解码器
 *
 * @author hanping
 * @date 2023-12-31
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * 构建适配自定义协议的帧解码器
     */
    public ProtocolFrameDecoder() {
        this(1024, 11, 4, 1, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
