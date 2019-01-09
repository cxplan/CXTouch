package com.cxplan.projection.core.connection;

import java.util.EventListener;

/**
 * Created on 2017/5/18.
 *
 * @author kenny
 */
public interface DeviceConnectionListener extends EventListener{

    /**
     * This method is invoked when a new image frame is coming.
     * A flag will be returned after invoked, this flag tells device connection whether the image channel
     * should be retained.
     *
     * @return true: the frame is consumed, false: the image is not consumed.
     */
    boolean frameReady(DeviceConnectionEvent event);
    /**
     * This method will be invoked when phone connection is created in context,
     * @param event connection event object.
     */
    void created(DeviceConnectionEvent event);

    /**
     * This method will be invoked when phone connection is removed from context,
     * @param event connection event object.
     */
    void removed(DeviceConnectionEvent event);

    /**
     * This method is invoked When node has connected the phone.
     * @param event
     */
    void connected(DeviceConnectionEvent event);

    /**
     * Notification that the connection was closed normally or that the reconnection
     * process has been aborted.
     */
    void connectionClosed(DeviceConnectionEvent event);

    /**
     * Notification that the device channel is changed. Usb cable and Wireless are supported by adb,
     * when the connection mode is changed, this method will be invoked.
     */
    void deviceChannelChanged(DeviceConnectionEvent event);

}
