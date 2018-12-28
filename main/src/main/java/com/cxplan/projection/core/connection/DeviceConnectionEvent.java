package com.cxplan.projection.core.connection;

import java.util.EventObject;

/**
 * Created on 2017/4/7.
 *
 * @author kenny
 */
public class DeviceConnectionEvent extends EventObject {

    public enum ConnectionType {
        IMAGE,
        MESSAGE,
        ADB,
        NETWORK
    }

    private ConnectionType type;
    private Object videoFrame;//This field is valid when

    public DeviceConnectionEvent(IDeviceConnection session, ConnectionType type) {
        super(session);
        this.type = type;
    }

    public IDeviceConnection getSource() {
        return (IDeviceConnection)super.getSource();
    }

    public ConnectionType getType() {
        return type;
    }

    public Object getVideoFrame() {
        return videoFrame;
    }

    public void setVideoFrame(Object videoFrame) {
        this.videoFrame = videoFrame;
    }
}
