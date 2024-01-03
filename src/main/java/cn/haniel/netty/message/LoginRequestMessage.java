package cn.haniel.netty.message;

import lombok.Data;
import lombok.ToString;

/**
 * 登录消息类型
 * @author hanping
 * @date 2023-12-31
 */
@Data
@ToString(callSuper = true)
public class LoginRequestMessage extends Message {

    private static final long serialVersionUID = -9216221007747875611L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    public LoginRequestMessage() {
    }

    public LoginRequestMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public int getMessageType() {
        return LOGIN_REQUEST_MESSAGE;
    }
}