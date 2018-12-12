package com.cxplan.projection.service;

/**
 * @author KennyLiu
 * @created on 2018/1/22
 */
public class MonkeyException extends RuntimeException {
    public MonkeyException() {
    }

    public MonkeyException(String message) {
        super(message);
    }

    public MonkeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonkeyException(Throwable cause) {
        super(cause);
    }

    public MonkeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
