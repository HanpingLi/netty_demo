package cn.haniel.netty.message;

import lombok.Data;
import lombok.ToString;

/**
 * @author hanping
 */
@Data
@ToString(callSuper = true)
public class ChatResponseMessage extends AbstractResponseMessage {

    private static final long serialVersionUID = 1038442164653109229L;

    private String from;
    private String content;

    public ChatResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    public ChatResponseMessage(String from, String content) {
        this.from = from;
        this.content = content;
    }

    @Override
    public int getMessageType() {
        return CHAT_RESPONSE_MESSAGE;
    }

    @Override
    public String toString() {
        return "ChatResponseMessage{" +
                "from='" + from + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}