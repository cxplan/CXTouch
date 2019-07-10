package com.cxplan.projection.script;

import com.cxplan.projection.script.io.ScriptDeviceConnection;

import java.util.EventObject;

/**
 * Created on 2017/4/7.
 *
 * @author kenny
 */
public class ScriptConnectionEvent extends EventObject {

    public ScriptConnectionEvent(ScriptDeviceConnection session) {
        super(session);
    }

    public ScriptDeviceConnection getSource() {
        return (ScriptDeviceConnection)super.getSource();
    }

}
