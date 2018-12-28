package com.cxplan.projection.net.message;

/**
 * Created on 2017/5/18.
 *
 * @author kenny
 */
public interface MessageListener {

    /**
     * Process the next packet sent to this packet listener.<p>
     *
     * A single thread is responsible for invoking all listeners, so
     * it's very important that implementations of this method not block
     * for any extended period of time.
     *
     * @param packet the packet to process.
     */
    void processPacket(Message packet);
}
