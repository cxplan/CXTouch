package com.cxplan.projection.core.connection;

/**
 * The call back listener for connecting.
 *
 * @author kenny
 * created on 2018-11-19
 */
public interface ConnectStatusListener {

    /**
     * This method will be invoked when connecting to device is successful.
     * @param connection the device connection.
     */
    void OnSuccess(IDeviceConnection connection);

    /**
     * This method will be invoked when connecting to device failed.
     * @param connection the device connection.
     * @param error error message.
     */
    void onFailed(IDeviceConnection connection, String error);
}