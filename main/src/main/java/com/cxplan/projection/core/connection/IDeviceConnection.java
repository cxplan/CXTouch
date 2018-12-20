package com.cxplan.projection.core.connection;

import com.android.ddmlib.IDevice;
import com.cxplan.projection.model.IDeviceMeta;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import org.apache.mina.core.session.IoSession;

import java.nio.channels.SocketChannel;

/**
 * @author KennyLiu
 * @created on 2017/9/15
 */
public interface IDeviceConnection extends IDeviceMeta {


    /**
     * Return the device SDK handle object.
     */
    IDevice getDevice();

    /**
     * Send message to device.
     *
     * @param message message object.
     * @throws MessageException
     */
    void sendMessage(Message message) throws MessageException;
    /**
     * Return the unique identifier of this connection.
     */
    String getId();
    /**
     * Return the channel connected to message service
     */
    IoSession getMessageSession();

    /**
     * Return a flag indicates whether the message channel is connected.
     */
    boolean isConnected();

    /**
     * Connect to message server run in device.
     */
    void connect();
    /**
     * Return flag indicates the image channel is available
     */
    boolean isImageChannelAvailable();

    /**
     * Open image channel.
     * If the image channel is not available, the connecting operation will be invoked,
     * and a false value will be returned. If the image channel is available, a true value
     * will be return.
     *
     * @return true: the image channel is connected already.
     *         false: the image channel is not available, a connecting operation will be executed.
     */
    boolean openImageChannel(ConnectStatusListener listener);

    /**
     * Close the image channel.
     */
    void closeImageChannel();

    SocketChannel getImageChannel();

    /**
     * Set a custom name for device.
     */
    void setDeviceName(String name);

    /**
     * Set the rate at which the screen size zooms.
     */
    void setZoomRate(float zoomRate);

    /**
     * Set the rotation of screen.
     */
    void setRotation(short rotation);
    /**
     * Return the meta object of device
     */
    IDeviceMeta getDeviceMeta();
}
