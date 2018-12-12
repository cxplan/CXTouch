/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.net.message;

/**
 * Created on 2017/6/30.
 * When user authorization is invalid, this exception will be thrown.
 *
 * @author kenny
 */
public class NotAuthException extends MessageException {

    public NotAuthException() {

    }

    public NotAuthException(String message) {
        super(message);
    }

    public NotAuthException(Throwable cause) {
        super(cause);
    }
}
