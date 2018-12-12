/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.net.message;

/**
 * Created on 2017/5/22.
 *
 * when operation from node is not allowed to executing, this exception will be thrown.
 *
 * @author kenny
 */
public class NoPermissionException extends MessageException {

    public NoPermissionException() {
    }

    public NoPermissionException(String message) {
        super(message);
    }
}
