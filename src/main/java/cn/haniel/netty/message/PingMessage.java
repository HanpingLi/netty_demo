package cn.haniel.netty.message;

import java.io.Serial;

/**
 * 心跳请求
 *
 * @author hanping
 */
public class PingMessage extends Message {

    @Serial
    private static final long serialVersionUID = 4813346140388001350L;

    @Override
    public int getMessageType() {
        return PING_MESSAGE;
    }
}