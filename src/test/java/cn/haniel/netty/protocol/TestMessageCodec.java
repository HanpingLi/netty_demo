package cn.haniel.netty.protocol;

import cn.haniel.netty.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 测试自定义协议编解码类
 *
 * @author hanping
 * @date 2023-12-31
 */
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(LogLevel.DEBUG),
                // 添加帧解码器，解决半包和黏包问题
//                new LengthFieldBasedFrameDecoder(1024, 11, 4, 1, 0),
                // 包一下帧解码器，避免写错
                new ProtocolFrameDecoder(),
                new MessageCodec()
        );

        // encode
        final LoginRequestMessage msg = new LoginRequestMessage("lisi", "lisi123456789");
        channel.writeOutbound(msg);

        // decode
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, msg, buf);

        channel.writeInbound(buf);
    }
}
