package com.cxplan.projection.net.message;

/**
 * 消息请求超时，该异常会抛出，发生在同步请求中。
 *
 * @author KennyLiu
 * @created on 2017/8/18
 */
public class MessageTimeoutException extends MessageException {
    public MessageTimeoutException() {
    }

    public MessageTimeoutException(String message) {
        super(message);
    }

    public MessageTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageTimeoutException(Throwable cause) {
        super(cause);
    }

    public MessageTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
