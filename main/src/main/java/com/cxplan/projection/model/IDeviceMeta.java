package com.cxplan.projection.model;

/**
 * @author KennyLiu
 * @created on 2018/5/18
 */
public interface IDeviceMeta {

    /**
     * Return the identifier of phone.
     */
    String getId();

    /**
     * Return the phone number in the phone.
     */
    String getPhone();

    /**
     * Return the phone alias name.
     */
    String getDeviceName();

    /**
     * Return the wifi that phone connected.
     */
    String getNetwork();
    /**
     * return the ip of this phone.
     */
    String getIp();

    /**
     * Return the port of video service.
     */
    int getVideoPort();
    /**
     * Return the width of video frame.
     * warning: the device size is according to horizontal orientation.
     */
    int getScreenWidth();
    /**
     * Return the height of video frame.
     * warning: the device size is according to horizontal orientation.
     */
    int getScreenHeight();

    /**
     * return the rate of zooming screen.
     */
    double getZoomRate();

    boolean isOnline();
    /**
     * Return the availability of network.
     */
    boolean isNetworkAvailable();

    /**
     * Return the manufacturer of device.
     */
    String getManufacturer();

    /**
     * Return the cpu architecture of device.
     */
    String getCpu();
    /**
     * Return the level of sdk.
     */
    String getApiLevel();

    /**
     * Return the model of device.
     */
    String getDeviceModel();

    /**
     * Return the version name of mediate app.
     */
    String getMediateVersion();

    /**
     * Return the version code of mediate app.
     */
    int getMediateVersionCode();
}
