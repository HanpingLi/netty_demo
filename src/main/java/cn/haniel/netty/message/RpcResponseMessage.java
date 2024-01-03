package cn.haniel.netty.message;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;

/**
 * RPC 响应消息
 *
 * @author hanping
 */
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {

    @Serial
    private static final long serialVersionUID = -2224316284363551066L;

    /**
     * 返回值
     */
    private Object returnValue;

    /**
     * 异常值
     */
    private String exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}