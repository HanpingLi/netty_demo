package cn.haniel.netty.protocol;

import cn.haniel.netty.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 对象可复用的自定义协议编解码器
 * 注意，需要确保和 LengthFixedBaseFrameDecoder 一起使用，确保当前编解码器接收到的 ByteBuf 是完整的
 *
 * @author hanping
 * @date 2023-12-31
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {

        final Serialize json = Serialize.JSON;
        final ByteBuf out = ctx.alloc().buffer();

        // 1. 魔数（4 字节）
        out.writeBytes(new byte[]{'C', 'A', 'F', 'E'});
        // 2. 版本号（1 字节）
        out.writeByte(1);
        // 3. 标识使用的序列化算法（1 字节）：0 - jdk，1 - json
        out.writeByte(json.getType());
        // 4. 消息（指令）类型（1 字节）
        out.writeByte(msg.getMessageType());
        // 5. 请求序号（4 字节）
        out.writeInt(msg.getSequenceId());

        // 序列化 msg 对象，作为内容填充到 ByteBuf，这里暂时使用 jdk
        final byte[] content = json.serialize(msg);

        // 6. 正文长度（4 字节）
        out.writeInt(content.length);

        // =============== 以上为固定字节，需要为 2 的倍数，不足则补齐
        // 对齐填充一个字节
        out.writeByte(0xff);

        // 7. 正文
        out.writeBytes(content);

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 1. 魔数（4 字节）
        final String magicStr = msg.readBytes(4).toString(StandardCharsets.UTF_8);
        // 2. 版本号（1 字节）
        final byte version = msg.readByte();
        // 3. 序列化算法（1 字节）
        final byte serializeType = msg.readByte();
        // 4. 消息类型（1 字节）
        final byte msgType = msg.readByte();
        // 5. 请求序号（4 字节）
        final int sequenceId = msg.readInt();
        // 6. 正文长度（4 字节）
        final int length = msg.readInt();
        // 读取对齐内容
        msg.readByte();

        // 7. 正文
        final byte[] content = new byte[length];
        msg.readBytes(content, 0, length);

        // 反序列化
        // 确定具体消息类型
        Class<? extends Message> messageClass = Message.getMessageClass(msgType);
        final Message message = Serialize.typeEnum(serializeType).deserialize(messageClass, content);
        out.add(message);

        log.info("{}, {}, {}, {}, {}, {}", magicStr, version, serializeType, msgType, sequenceId, length);
        log.info("{}", message);
    }
}
