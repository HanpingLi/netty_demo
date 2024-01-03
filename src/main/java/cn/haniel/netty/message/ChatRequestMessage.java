package cn.haniel.netty.message;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;

/**
 * 聊天请求消息
 *
 * @author hanping
 */
@Data
@ToString(callSuper = true)
public class ChatRequestMessage extends Message {

    @Serial
    private static final long serialVersionUID = -4962526575452161323L;

    /**
     * 聊天内容
     */
    private String content;

    /**
     * 聊天对象
     */
    private String to;
    /**
     * 聊天发起方
     */
    private String from;

    public ChatRequestMessage() {
    }

    public ChatRequestMessage(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
    }

    @Override
    public int getMessageType() {
        return CHAT_REQUEST_MESSAGE;
    }

    @Override
    public String toString() {
        return "ChatRequestMessage{" +
                "content='" + content + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}