package com.cxplan.projection.service;

import com.android.ddmlib.IDevice;


/**
 * @author KennyLiu
 * @created on 2018/6/11
 */
public interface IInfrastructureService {

    /**
     * Check whether the device mediate package is installed.
     * @return true: install already, false: not install.
     */
    boolean checkMainPackageInstallation(String deviceId);

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
     * @param width the width of device screen.
     * @param height the height of device screen.
     * @param scale the rate of zooming out/in device screen.
     */
    void startMinicapService(String deviceId, int width, int height, double scale);

    /**
     * Set specified inputer as default inputer on device.
     * @param deviceId device ID.
     * @param inputerId the id of inputer.
     */
    void switchInputer(String deviceId, String inputerId);

}
