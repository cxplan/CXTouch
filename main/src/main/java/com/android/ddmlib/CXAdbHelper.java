package com.android.ddmlib;

import com.cxplan.projection.core.Application;
import com.cxplan.projection.core.adb.AdbUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author KennyLiu
 * @created on 2018/5/18
 */
public class CXAdbHelper {

    public static void removeAllForward() throws Exception{
        InetSocketAddress adbSockAddr = AndroidDebugBridge.getSocketAddress();

        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);

            byte[] request = AdbHelper.formAdbRequest("host-serial:killforward-all");

            AdbHelper.write(adbChan, request);

            AdbHelper.AdbResponse resp = AdbHelper.readAdbResponse(adbChan, false /* readDiagString */);
            if (!resp.okay) {
                Log.w("CXADBHelper", "Error creating forward: " + resp.message);
                throw new AdbCommandRejectedException(resp.message);
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
    }

    /**
     * The implementation of command 'adb tcpip port'
     * @param port the service port of adbd.
     * @throws Exception
     */
    public static void tcpip(int port, IDevice device) throws Exception {
        if (port < 1) {
            throw new IllegalArgumentException("The port is illegal: " + port);
        }
        InetSocketAddress adbSockAddr = AndroidDebugBridge.getSocketAddress();

        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);

            byte[] request = AdbHelper.formAdbRequest("tcpip:" + port);
            AdbHelper.setDevice(adbChan, device);
            AdbHelper.write(adbChan, request);

            AdbHelper.AdbResponse resp = AdbHelper.readAdbResponse(adbChan, false /* readDiagString */);
            if (!resp.okay) {
                Log.w("CXADBHelper", "Error tcpip: " + resp.message);
                throw new AdbCommandRejectedException(resp.message);
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
    }

    /**
     * The implementation of command 'adb connect deviceIP'
     * @param host the ip address of device.
     * @param port the port of adbd service.
     * @throws Exception
     */
    public static void connect(String host, int port) throws Exception {
        InetSocketAddress adbSockAddr = AndroidDebugBridge.getSocketAddress();

        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);

            byte[] request = AdbHelper.formAdbRequest("host:connect:" + host + ":" + port);

            AdbHelper.write(adbChan, request);

            AdbHelper.AdbResponse resp = AdbHelper.readAdbResponse(adbChan, false /* readDiagString */);
            if (!resp.okay) {
                Log.w("CXADBHelper", "Error connect: " + resp.message);
                throw new AdbCommandRejectedException(resp.message);
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
    }

    /**
     * The implementation of command 'adb disconnect deviceIP'
     * @param host the ip address of device.
     * @param port the port of adbd service.
     * @throws Exception
     */
    public static void disconnect(String host, int port) throws Exception {
        InetSocketAddress adbSockAddr = AndroidDebugBridge.getSocketAddress();

        SocketChannel adbChan = null;
        try {
            adbChan = SocketChannel.open(adbSockAddr);
            adbChan.configureBlocking(false);

            byte[] request = AdbHelper.formAdbRequest("host:disconnect:" + host + ":" + port);

            AdbHelper.write(adbChan, request);

            AdbHelper.AdbResponse resp = AdbHelper.readAdbResponse(adbChan, false /* readDiagString */);
            if (!resp.okay) {
                Log.w("CXADBHelper", "Error connect: " + resp.message);
                throw new AdbCommandRejectedException(resp.message);
            }
        } finally {
            if (adbChan != null) {
                adbChan.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //start ADB service
        String adbLocation = System.getenv("ANDROID_HOME");
        if (adbLocation != null) {
            adbLocation += File.separator + "platform-tools" + File.separator + "adb";
        } else {
            adbLocation = "adb";
        }

        AndroidDebugBridge.init(false);
        final AndroidDebugBridge adb = AndroidDebugBridge.createBridge(adbLocation, false);
        try {
            System.out.println("wait 1.2 seconds!");
            Thread.sleep(1200);
        } catch (InterruptedException e) {
        }

        System.out.println("The connection state of ADB: " + adb.isConnected());
        if (adb.isConnected()) {
            System.out.println("The number of devices: " + adb.getDevices().length);
        }

        AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {
            public void deviceConnected(IDevice device) {
                if (device.isOnline()) {
                    String id = AdbUtil.getDeviceId(device);
                    System.out.println("device connected:"+ id);
                }
            }

            public void deviceDisconnected(IDevice device) {
                String id = AdbUtil.getDeviceId(device);
                System.out.println("device disconnected:"+ id);
            }

            public void deviceChanged(IDevice device, int changeMask) {
                System.out.println("changed:" + device.isOnline() + ":" + device);
            }
        });

        System.out.println("count:"+ adb.getDevices().length);
        IDevice device = adb.getDevices()[0];
        System.out.println(device);
        String ip = AdbUtil.getDeviceIp(device);

        CXAdbHelper.tcpip(5555, device);
        AdbUtil.getDeviceIp(device);
        Thread.sleep(2000);

        System.out.println("new count: " + adb.getDevices().length);
//        CXAdbHelper.connect(ip, 5555);
        AdbUtil.getDeviceIp(device);

        System.out.println("remove all is ok");

    }
}
