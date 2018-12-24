package com.cxplan.projection.service;

import com.cxplan.projection.net.message.MessageException;

import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author KennyLiu
 * @created on 2018/10/27
 */
public interface IDeviceService {
    /**
     * Send a touch (down and then up) event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     */
    void touch(String deviceId, int x, int y) throws MessageException;

    /**
     * Send a touch down event at the specified location.
     *
     * @param x the x coordinate of where to click
     * @param y the y coordinate of where to click
     */
    void touchDown(String deviceId, int x, int y) throws MessageException;
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
    /**
     * Press a physical button on the device.
     *
     * @param keycode the key code of the button (As specified in the protocol)
     */
    void press(String deviceId, int keycode) throws MessageException;
    /**
     * Send a Key Down event for the specified button.
     *
     * @param name the name of the button (As specified in the protocol)
     */
    void keyDown(String deviceId, String name) throws MessageException;
    /**
     * Send a Key Up event for the specified button.
     *
     * @param name the name of the button (As specified in the protocol)
     */
    void keyUp(String deviceId, String name) throws  MessageException;

    void reboot(String deviceId, String into) throws MessageException;

    void back(String deviceId) throws MessageException;

    /**
     * Unlock the screen of device.
     * @param deviceId the ID of device.
     *
     */
    void wake(String deviceId) throws MessageException;
    /**
     * Start an activity.
     *
     * @param deviceId The id of device.
     * @param uri the URI for the Intent
     * @param action the action for the Intent
     * @param data the data URI for the Intent
     * @param mimeType the mime type for the Intent
     * @param categories the category names for the Intent
     * @param extras the extras to add to the Intent
     * @param component the component of the Intent
     * @param flags the flags for the Intent
     */
    void startActivity(String deviceId, String uri, String action,
                       String data, String mimeType,
                       Collection<String> categories, Map<String, Object> extras, String component,
                       int flags) throws MessageException;

    void scrollUp(String deviceId) throws MessageException;
    void scrollDown(String deviceId) throws MessageException;
    /**
     * toggleScreenOnOff specified device.
     */
    void toggleScreenOnOff(String deviceId) throws MessageException;
    /**
     * change screen to home for specified device.
     */
    void home(String deviceId) throws MessageException;
    /**
     * Reboot specified device.
     */
    void reboot(String deviceId) throws MessageException;

    /**
     * start weixin application in specified device.
     */
    void startApp(String deviceId) throws MessageException;

    /**
     * Input text to specified device
     */
    void type(String deviceId, String text);

    /**
     * Install apk file to device.
     * @return true: install successfully, false: failed.
     */
    boolean installPackage(List<File> fileList, String... deviceId) throws MessageException;

    /**
     * Pulls a single file.
     *
     * @param deviceId The device ID
     * @param remote the full path to the remote file
     * @param local The local destination.
     */
    void pullFile(String deviceId, String remote, String local) throws MessageException;

    /**
     * Synchronize the contact in device to controller.
     */
    int syncContact(String deviceId) throws MessageException;
    /**
     * Synchronize the message in device to controller.
     * @param startTime The min time, null mean no limit
     * @param endTime The max time ,null mean no limit
     * @return The information about synchronizing message.
     */
    String syncMessage(String deviceId, Date startTime, Date endTime) throws MessageException;

    String shell(String deviceId, String cmd) throws MessageException;
    /**
     * Execute shell command on specified device, you can define a time out (milliseconds)
     */
    String shell(String deviceId, String cmd, int timeout) throws MessageException;

    /**
     * Locate specified device to find it conveniently.
     * @param deviceId the device ID.
     */
    void locateDevice(String deviceId);

    /**
     * Take a screenshot image of device.
     * @param deviceId the device ID.
     * @param zoomRate the rate of zooming, the range of value is 0.0 - 1.0.
     * @param quality the quality of image, the range of value is 0 - 100.
     */
    Image takeScreenshot(String deviceId, float zoomRate, int quality) throws MessageException;
}
