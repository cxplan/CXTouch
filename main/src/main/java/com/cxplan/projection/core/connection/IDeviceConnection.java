package com.cxplan.projection.core.connection;

import com.cxplan.projection.model.IDeviceMeta;
import org.apache.mina.core.session.IoSession;

import java.nio.channels.SocketChannel;

/**
 * @author KennyLiu
 * @created on 2017/9/15
 */
public interface IDeviceConnection extends IDeviceMeta {

    String PROPERTY_WIDTH = "width";
    String PROPERTY_HEIGHT = "height";
    String PROPERTY_ZOOM_RATE = "zoom_rate";
    String PROPERTY_PHONE = "phone";
    String PROPERTY_DEVICE_NAME = "device_name";
    int MAX_TOP_USERS = 5;

    IDeviceMeta getDeviceMeta();

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
     * return the thread of process video frame.
     */
    Thread getImageProcessThread();

    void setDeviceName(String name);
}
