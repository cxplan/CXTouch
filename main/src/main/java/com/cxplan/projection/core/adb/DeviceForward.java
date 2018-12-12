package com.cxplan.projection.core.adb;

/**
 * @author KennyLiu
 * @created on 2018/5/4
 */
public class DeviceForward {

    private String id;
    private int localPort;
    private int remotePort;
    private String remoteSocketName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getRemoteSocketName() {
        return remoteSocketName;
    }

    public void setRemoteSocketName(String remoteSocketName) {
        this.remoteSocketName = remoteSocketName;
    }
}
