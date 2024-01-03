package cn.haniel.netty.message;

import lombok.Data;
import lombok.ToString;

/**
 * 响应体公共类
 *
 * @author hanping
 */
@Data
@ToString(callSuper = true)
public abstract class AbstractResponseMessage extends Message {

    private static final long serialVersionUID = 3311074573932822815L;
    /**
     * 状态码，是否成功
     */
    private boolean success;

    /**
     * 原因
     */
    private String reason;

    public AbstractResponseMessage() {
    }

    public AbstractResponseMessage(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }
}