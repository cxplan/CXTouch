package com.cxplan.projection.script.command;

/**
 * When some errors occur on playing script, this exception will be thrown.
 *
 * @author Kenny
 * created on 2019/3/19
 */
public class ScriptException extends Exception {

    public ScriptException() {
    }

    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptException(Throwable cause) {
        super(cause);
    }

    public ScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
