package com.cxplan.projection.core;

import com.cxplan.projection.core.connection.ClientConnection;
import com.cxplan.projection.core.connection.MessageCollector;
import com.cxplan.projection.net.message.Message;

/**
 * @author Kenny
 * created on 2019/3/22
 */
public class BaseDeviceConnection extends ClientConnection {

    public boolean visitMessageCollectors(Message message) {
        boolean ret = false;
        for (MessageCollector collector : getPacketCollectors()) {
            if (processMessage(collector, message)) {
                ret = true;
            }
        }

        return ret;
    }
}
