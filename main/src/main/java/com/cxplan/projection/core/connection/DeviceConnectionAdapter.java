package com.cxplan.projection.core.connection;

/**
 * @author KennyLiu
 * @created on 2017/9/11
 */
public class DeviceConnectionAdapter implements DeviceConnectionListener {
    @Override
    public boolean frameReady(DeviceConnectionEvent event) {
        return false;
    }

    @Override
    public void created(DeviceConnectionEvent event) {

    }

    @Override
    public void removed(DeviceConnectionEvent event) {

    }

    @Override
    public void connected(DeviceConnectionEvent event) {

    }

    @Override
    public void connectionClosed(DeviceConnectionEvent event) {

    }

    @Override
    public void deviceChannelChanged(DeviceConnectionEvent event) {

    }

}
