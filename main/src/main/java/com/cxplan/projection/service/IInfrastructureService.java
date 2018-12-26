package com.cxplan.projection.service;

import com.android.ddmlib.IDevice;
import com.cxplan.projection.core.adb.RecordMeta;
import com.cxplan.projection.net.message.MessageException;


/**
 * @author KennyLiu
 * @created on 2018/6/11
 */
public interface IInfrastructureService {

    /**
     * Return the installed path of main process package,
     * a null value will be returned if the main process package is not installed.
     */
    String getMainPackageInstallPath(String deviceId);

    /**
     * Install main process application to specified device.
     *
     * @param device the device object.
     */
    void installMainProcess(IDevice device);
    /**
     * Start main process on specified device.
     */
    void startMainProcess(IDevice device);
    /**
     * Install suitable version to specified device.
     */
    void installMinicap(String deviceId);

    /**
     * Check whether the minicap is installed on specified device completely.
     */
    boolean checkMinicapInstallation(String deviceId);
    /**
     * Return the process ID of minicap on specified device.
     * If the program is not started, -1 will be returned.
     */
    int getMinicapProcessID(String deviceId);

    /**
     * Start minicap service on specified device.
     */
    void startMinicapService(String deviceId) throws MessageException;

    /**
     * Set specified inputer as default inputer on device.
     * @param deviceId device ID.
     * @param inputerId the id of inputer.
     */
    void switchInputer(String deviceId, String inputerId);

    /**
     * Notify device the projection work is in progress.
     * @param deviceId device ID.
     * @param inProjection the flag whether the projection work is in progress.
     */
    void notifyProjectionFlag(String deviceId, boolean inProjection);

    /**
     * Start the task of recording device screen.
     * A zooming rate can be specified to control
     *
     * @param deviceId the device ID.
     * @param zoomRate the rate of zooming.
     */
    void startRecord(String deviceId, float zoomRate) throws MessageException;

    /**
     * Stop the task of recording screen, and return the path of remote video file in device.
     *
     * @param deviceId device ID.
     * @return the path of remote video file in device.
     */
    RecordMeta stopRecord(String deviceId) throws MessageException;

}
