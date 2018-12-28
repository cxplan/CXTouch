package com.cxplan.projection.net.message;

/**
 * Created on 2017/5/23.
 * If the 'to' of message object isn't connected to controller, this message will be discarded.
 * The client will receive error type, and then this exception will be thrown.
 *
 * @author kenny
 */
public class TargetNotFoundException extends MessageException {

    public TargetNotFoundException() {
    }

    public TargetNotFoundException(String message) {
        super(message);
    }

}
