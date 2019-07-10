package com.cxplan.projection.script;

import java.util.EventListener;

/**
 * Created on 2017/5/18.
 *
 * @author kenny
 */
public interface ScriptConnectionListener extends EventListener{

    /**
     * This method is invoked When node has connected the phone.
     * @param event
     */
    void connected(ScriptConnectionEvent event);

    /**
     * Notification that the connection was closed normally or that the reconnection
     * process has been aborted.
     */
    void connectionClosed(ScriptConnectionEvent event);

}
