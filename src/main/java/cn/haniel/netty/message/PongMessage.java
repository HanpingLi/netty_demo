package cn.haniel.netty.message;

import java.io.Serial;

/**
 * @author hanping
 */
public class PongMessage extends Message {

    @Serial
    private static final long serialVersionUID = 3873733926105272389L;

    @Override
    public int getMessageType() {
        return PONG_MESSAGE;
    }
}