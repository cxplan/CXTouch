/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created on 2017/5/17.
 *
 * @author kenny
 */
public class DeviceInfo implements IDeviceMeta {

    public static final long serialVersionUID = 111111111L;

    private String id;
    private String phone;
    private String deviceName;
    private String network;
    private boolean networkAvailable;
    private String ip;
    private int port;//video service port

    private int screenWidth;
    private int screenHeight;
    private double zoomRate;

    private boolean isOnline;
    private String manufacturer;
    private String cpu;
    private String apiLevel;
    private String deviceModel;
    private String mediateVersion;
    private int mediateVersionCode;

    public DeviceInfo() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @JsonProperty("p")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("p")
    public void setPhone(String phone) {
        this.phone = phone;
    }
    @JsonProperty("wf")
    public String getNetwork() {
        return network;
    }
    @JsonProperty("wf")
    public void setNetwork(String network) {
        this.network = network;
    }
    @JsonProperty("pn")
    public String getDeviceName() {
        return deviceName;
    }
    @JsonProperty("pn")
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIp() {
        return ip;
    }

    @Override
    @JsonProperty("po")
    public int getVideoPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    @JsonProperty("po")
    public void setVideoPort(int port) {
        this.port = port;
    }
    @JsonProperty("vw")
    public int getScreenWidth() {
        return screenWidth;
    }
    @JsonProperty("vw")
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }
    @JsonProperty("vh")
    public int getScreenHeight() {
        return screenHeight;
    }
    @JsonProperty("vh")
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
    @JsonProperty("zr")
    public double getZoomRate() {
        return zoomRate;
    }
    @JsonProperty("zr")
    public void setZoomRate(double zoomRate) {
        this.zoomRate = zoomRate;
    }
    @JsonProperty("o")
    public boolean isOnline() {
        return isOnline;
    }
    @JsonProperty("o")
    public void setOnline(boolean online) {
        isOnline = online;
    }
    @JsonProperty("na")
    @Override
    public boolean isNetworkAvailable() {
        return networkAvailable;
    }
    @JsonProperty("na")
    public void setNetworkAvailable(boolean networkAvailable) {
        this.networkAvailable = networkAvailable;
    }

    @Override
    @JsonProperty("mf")
    public String getManufacturer() {
        return manufacturer;
    }
    @JsonProperty("mf")
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    @JsonProperty("cpu")
    @Override
    public String getCpu() {
        return cpu;
    }
    @JsonProperty("cpu")
    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    @Override
    @JsonProperty("al")
    public String getApiLevel() {
        return apiLevel;
    }
    @JsonProperty("al")
    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
    }

    @Override
    @JsonProperty("dm")
    public String getDeviceModel() {
        return deviceModel;
    }
    @JsonProperty("dm")
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    @Override
    public String getMediateVersion() {
        return mediateVersion;
    }

    public void setMediateVersion(String mediateVersion) {
        this.mediateVersion = mediateVersion;
    }

    @Override
    public int getMediateVersionCode() {
        return mediateVersionCode;
    }

    public void setMediateVersionCode(int mediateVersionCode) {
        this.mediateVersionCode = mediateVersionCode;
    }
}
