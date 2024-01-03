package cn.haniel.netty.message;

import lombok.Data;
import lombok.ToString;

/**
 * 登录响应消息
 *
 * @author hanping
 */
@Data
@ToString(callSuper = true)
public class LoginResponseMessage extends AbstractResponseMessage {

    private static final long serialVersionUID = -8911423762092850713L;

    public LoginResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    @Override
    public int getMessageType() {
        return LOGIN_RESPONSE_MESSAGE;
    }
}