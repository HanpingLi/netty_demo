package cn.haniel.netty.protocol;

import cn.haniel.netty.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 消息的自定义编解码器
 * <p>
 * 该自定义编解码器继承自 Netty 的 ByteToMessageCodec，用于将 ByteBuf 和自定义消息类型互相编解码
 *
 * @author hanping
 * @date 2023-12-31
 * @deprecated use MessageCodecSharable
 */
@Slf4j
@Deprecated(since = "MessageCodecSharable")
public class MessageCodec extends ByteToMessageCodec<Message> {
    /**
     * 将 msg 安装自定义协议的格式，写入 ByteBuf 即可
     *
     * @param ctx ctx
     * @param msg 自定义消息
     * @param out 传输出去的 ByteBuf
     * @throws Exception 异常
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 1. 魔数（4 字节）
        out.writeBytes(new byte[]{'C', 'A', 'F', 'E'});
        // 2. 版本号（1 字节）
        out.writeByte(1);
        // 3. 标识使用的序列化算法（1 字节）：0 - jdk，1 - json
        out.writeByte(0);
        // 4. 消息（指令）类型（1 字节）
        out.writeByte(msg.getMessageType());
        // 5. 请求序号（4 字节）
        out.writeInt(msg.getSequenceId());

        // 序列化 msg 对象，作为内容填充到 ByteBuf，这里暂时使用 jdk
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        final byte[] content = bos.toByteArray();

        // 6. 正文长度（4 字节）
        out.writeInt(content.length);

        // =============== 以上为固定字节，需要为 2 的倍数，不足则补齐
        // 对齐填充一个字节
        out.writeByte(0xff);

        // 7. 正文
        out.writeBytes(content);
    }

    /**
     * 将 ByteBuf 的内容解码成自定义消息对象 Message，并存入 out
     *
     * @param ctx ctx
     * @param in  传入的 ByteBuf
     * @param out 承载解析好的自定义对象
     * @throws Exception 异常
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 1. 魔数（4 字节）
        final String magicStr = in.readBytes(4).toString(StandardCharsets.UTF_8);
        // 2. 版本号（1 字节）
        final byte version = in.readByte();
        // 3. 序列化算法（1 字节）
        final byte serializeType = in.readByte();
        // 4. 消息类型（1 字节）
        final byte msgType = in.readByte();
        // 5. 请求序号（4 字节）
        final int sequenceId = in.readInt();
        // 6. 正文长度（4 字节）
        final int length = in.readInt();
        // 读取对齐内容
        in.readByte();

        // 7. 正文
        final byte[] content = new byte[length];
        in.readBytes(content, 0, length);

        // 反序列化
        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(content));
        final Message message = (Message) ois.readObject();
        out.add(message);

        log.info("{}, {}, {}, {}, {}, {}", magicStr, version, serializeType, msgType, sequenceId, length);
        log.info("{}", message);
    }
}
