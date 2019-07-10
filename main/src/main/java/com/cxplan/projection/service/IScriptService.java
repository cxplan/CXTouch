package com.cxplan.projection.service;

import com.android.ddmlib.IDevice;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.script.ViewNode;

/**
 * The interface for Script service
 *
 * @author kenny
 * Created on 2019-3-23
 */
public interface IScriptService {

    /**
     * Return the installed path of script application package,
     * a null value will be returned if the script application package is not installed.
     */
    String getScriptPackageInstallPath(String deviceId);

    /**
     * Return the versionCode of script application in specified device.
     * @param deviceId the device ID.
     * @return version code.
     */
    int getScriptPackageVersion(String deviceId);
    /**
     * Install script application to specified device.
     *
     * @param deviceId the device ID.
     */
    void installScriptService(String deviceId);
    /**
     * Start script service on specified device.
     *
     * @return true: A script process has existed already, false: Create new script process.
     */
    boolean startScriptService(IDevice device);

    /**
     * Span component according to coordinates, return all properties of component.
     *
     * @param deviceId the device ID.
     * @param x x coordinate.
     * @param y y coordinate.
     * @return Return the view node located at specified coordinates.
     * @throws MessageException
     */
    ViewNode spanComponent(String deviceId, int x, int y) throws MessageException;

    /**
     * Dump the hierarchy of views on specified device.
     *
     * @param deviceId device ID.
     * @throws MessageException
     */
    byte[] dumpHierarchy(String deviceId) throws MessageException;

    //---------------------monkey operation----------------------------
    /**
     * Send a touch down event at the specified location.
     * This action will record the view on screen which will be clicked.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     * @param seqNum the sequence number of this touch down event in recorder script.
     *               This field is considered as key ID to link view and script command.
     */
    void touchDown(String deviceId, int x, int y, int seqNum) throws MessageException;
    /**
     * Send a touch down event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     */
    void touchUp(String deviceId, int x, int y) throws MessageException;
    /**
     * Send a touch move event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     */
    void touchMove(String deviceId, int x, int y) throws MessageException;
    //---------------------monkey operation----------------------------

    /**
     * Wait device idle. This method will be blocked util a idle event is coming.
     *
     * @param deviceId the device ID
     * @param timeout The timeout wait for idle event in milliseconds
     * @return true: a idle event occurred, false: waiting is timeout.
     */
    boolean waitIdle(String deviceId, long timeout) throws MessageException;

    /**
     * Wait specified view presented on screen.
     * When the device window is idle, Specified view is expected to be presented on screen within timeout.
     * If it's timeout, the waiting operation wil be interrupted and return immediately.
     *
     * @param deviceId the device ID
     * @param view The view component should be presented on screen.
     * @param timeout The timeout waited for presenting view in milliseconds
     * @return  1: The view is presented on screen now.
     *          2: The view is not found after device is idle.
     *          3: The view is not found because waiting device idle is timeout.
     * @throws MessageException
     */
    int waitForView(String deviceId, ViewNode view, long timeout) throws MessageException;

}
