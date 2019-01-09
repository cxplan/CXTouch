package com.cxplan.projection.core.connection;

/**
 * Handles the automatic reconnection process. Every time a connection is dropped without
 * the application explicitly closing it, the manager automatically tries to reconnect to
 * the device server.<p>
 *
 */
public class DeviceReconnectionManager extends ReconnectionManager implements DeviceConnectionListener {

    public DeviceReconnectionManager() {
        super("device");
    }

    @Override
    public void connected(DeviceConnectionEvent event) {
        removeReconnect((ClientConnection) event.getSource(), true);
    }

    @Override
    public void connectionClosed(DeviceConnectionEvent event) {
        if (event.getType() == DeviceConnectionEvent.ConnectionType.MESSAGE) {
            addConnection((ClientConnection) event.getSource());
        }
    }

    @Override
    public void deviceChannelChanged(DeviceConnectionEvent event) {

    }

    @Override
    public boolean frameReady(DeviceConnectionEvent event) {
        return false;
    }

    @Override
    public void created(DeviceConnectionEvent event) {

    }

    @Override
    public void removed(DeviceConnectionEvent event) {
        removeReconnect((ClientConnection) event.getSource(), false);
    }
}