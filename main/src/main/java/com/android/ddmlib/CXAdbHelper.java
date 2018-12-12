package com.android.ddmlib;

import java.io.File;
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
                Log.w("remove-forward", "Error creating forward: " + resp.message);
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
        CXAdbHelper.removeAllForward();
        System.out.println("remove all is ok");

    }
}
