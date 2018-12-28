package com.cxplan.projection.service;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.cxplan.projection.MonkeyConstant;
import com.cxplan.projection.core.CXService;
import com.cxplan.projection.core.DefaultDeviceConnection;
import com.cxplan.projection.core.adb.AdbUtil;
import com.cxplan.projection.core.adb.RecordMeta;
import com.cxplan.projection.core.connection.ClientConnection;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.core.setting.SettingConstant;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.util.CommonUtil;
import com.cxplan.projection.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KennyLiu
 * @created on 2018/6/11
 */
@CXService("infrastructureService")
public class ControllerInfrastructureService extends BaseBusinessService implements IInfrastructureService {
    public static final String PROP_ABI = "ro.product.cpu.abi";
    public static final String PROP_SDK = "ro.build.version.sdk";

    private static final Logger logger = LoggerFactory.getLogger(ControllerInfrastructureService.class);

    private Map<String, Thread> mainThreadMap = new ConcurrentHashMap<>();

    @Override
    public String getMainPackageInstallPath(String deviceId) {
        DefaultDeviceConnection connection = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }
        String mainPackagePath = AdbUtil.getPackagePath(CommonUtil.PACKAGE_MAIN, connection.getDevice());
        return mainPackagePath;
    }

    @Override
    public int getMainPackageVersion(String deviceId) {
        DefaultDeviceConnection connection = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }
        int versionCode = AdbUtil.getPackageVersionCode(CommonUtil.PACKAGE_MAIN, connection.getDevice());
        return versionCode;
    }

    @Override
    public void installMainProcess(IDevice device) {
        try {
            device.installPackage("res/mediate/CXTouch.apk", true);
        } catch (InstallException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void startMainProcess(final IDevice device) {

        int pid = checkMainProcess(device);
        if (pid > 0) {
            logger.info("The main process existed already: " + pid);
            return;
        }

        final String deviceId = AdbUtil.getDeviceId(device);
        final String cmd = buildMainCmd(device);
        synchronized (mainThreadMap) {
            if (mainThreadMap.containsKey(deviceId)) {
                return;
            }
            Thread th = new Thread("MainPackage Thread-" + deviceId) {

                @Override
                public void run() {
                    try {
                        AdbUtil.shellBlockingCommand(cmd, device, 0);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    mainThreadMap.remove(deviceId);
                    logger.info("The main process is finished: {}", deviceId);
                }
            };
            th.start();
            mainThreadMap.put(deviceId, th);
        }
    }

    @Override
    public void installMinicap(String deviceId) {
        DefaultDeviceConnection connection = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        //1. check sdk and ABI
        String sdk = connection.getDevice().getProperty(PROP_SDK);
        String abi = connection.getDevice().getProperty(PROP_ABI);
        if (StringUtil.isBlank(sdk) || StringUtil.isBlank(abi)) {
            throw new RuntimeException("The android context can't be retrieved. sdk=" + sdk + ", abi=" + abi);
        }
        sdk = sdk.trim();
        abi = abi.trim();

        //2.push files to device
        StringBuilder sb = new StringBuilder();
        sb.append("res/minicap/bin/").append(abi).append("/minicap");
        File binFile = new File(sb.toString());
        if (!binFile.exists()) {
            throw new RuntimeException("The minicap bin file doesn't exist: " + binFile.getAbsolutePath());
        }
        sb.setLength(0);
        sb.append("res/minicap/lib/android-").append(sdk).append("/").append(abi).append("/minicap.so");
        File shareFile = new File(sb.toString());
        if (!shareFile.exists()) {
            throw new RuntimeException("The minicap share file doesn't exist: " + shareFile.getAbsolutePath());
        }
        try {
            connection.getDevice().pushFile(binFile.getAbsolutePath(), "/data/local/tmp/minicap");
            connection.getDevice().pushFile(shareFile.getAbsolutePath(), "/data/local/tmp/minicap.so");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        //3. assign executable permission to file.
        AdbUtil.shell("chmod 777 /data/local/tmp/minicap", connection.getDevice());
    }

    @Override
    public boolean checkMinicapInstallation(String deviceId) {
        DefaultDeviceConnection connection = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        String cmd = "ls /data/local/tmp/minicap /data/local/tmp/minicap.so";
        String ret = AdbUtil.shell(cmd, connection.getDevice());
        StringTokenizer st = new StringTokenizer(ret);

        int index = 0;
        while (st.hasMoreElements()) {
            String token = st.nextToken();

            if (index == 0) {
                if (token.equals("/data/local/tmp/minicap")) {
                    index++;
                    continue;
                } else {
                    return false;
                }
            } else if(index == 1) {
                if (token.equals("/data/local/tmp/minicap.so")) {
                    index++;
                    continue;
                } else {
                    return false;
                }
            } else {
                logger.error("Unexpected token: " + token + ", the whole result:" + ret);
                return false;
            }
        }

        return index == 2;
    }

    @Override
    public int getMinicapProcessID(String deviceId) {
        IDeviceConnection connection = application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        String cmd = AdbUtil.getPsCommand(connection.getDevice()) + "|grep minicap";
        String ret;
        try {
            ret = AdbUtil.shell(cmd, connection.getDevice());
            if (StringUtil.isEmpty(ret)) {
                return -1;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
        ret = ret.trim();
        return CommonUtil.resolveProcessID(ret, "/data/local/tmp/minicap");
    }

    @Override
    public void startMinicapService(final String deviceId) throws MessageException{
        final IDeviceConnection connection = application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        int imageQuality = Setting.getInstance().getIntProperty(deviceId,
                SettingConstant.KEY_DEVICE_IMAGE_QUALITY, SettingConstant.DEFAULT_IMAGE_QUALITY);
        float zoomRate = Setting.getInstance().getFloatProperty(deviceId,
                SettingConstant.KEY_DEVICE_IMAGE_ZOOM_RATE, SettingConstant.DEFAULT_ZOOM_RATE);
        Message message = new Message(MessageUtil.CMD_DEVICE_IMAGE);
        message.setParameter("type", (short)3);
        message.setParameter("iq", imageQuality);
        message.setParameter("zr", zoomRate);

        MessageUtil.request((ClientConnection) connection, message, 5000);
    }

    @Override
    public void switchInputer(String deviceId, String inputerId) {
        IDeviceConnection connection = application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }
        if (StringUtil.isEmpty(inputerId)) {
            return;
        }

        //enable
        String cmd = "ime enable " + inputerId;
        try {
            AdbUtil.shell(cmd, connection.getDevice());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }
        //set
        cmd = "ime set " + inputerId;
        try {
            AdbUtil.shell(cmd, connection.getDevice());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }

        //notify mediate
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("isTouchIME", CommonUtil.TOUCH_INPUTER.equals(inputerId));
        message.setParameter("type", MonkeyConstant.MONKEY_SWITCH_INPUTER);
        try {
            connection.sendMessage(message);
        } catch (MessageException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void notifyProjectionFlag(String deviceId, boolean inProjection) {
        IDeviceConnection connection = application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_IMAGE);
        message.setParameter("type", inProjection ? (short)1 : (short)2);
        try {
            connection.sendMessage(message);
        } catch (MessageException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void startRecord(String deviceId, float zoomRate) throws MessageException {
        final IDeviceConnection connection = application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_IMAGE);
        message.setParameter("type", (short)5);
        message.setParameter("zr", zoomRate);

        MessageUtil.request((ClientConnection) connection, message, 5000);
    }

    @Override
    public RecordMeta stopRecord(String deviceId) throws MessageException {
        final IDeviceConnection connection = application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_IMAGE);
        message.setParameter("type", (short)6);

        Message ret = MessageUtil.request((ClientConnection) connection, message, 5000);
        int status = ret.getParameter("ret");
        if (status == 0) {
            throw new MessageException(ret.getError());
        }
        String file = ret.getParameter("file");
        long size = ret.getParameter("size");
        return new RecordMeta(file, size);
    }

    private int checkMainProcess(IDevice device) {
        String cmd = AdbUtil.getPsCommand(device) + "|grep " + CommonUtil.PROCESS_NAME_MAIN;
        String ret;
        try {
            ret = AdbUtil.shell(cmd, device);
            if (StringUtil.isEmpty(ret)) {
                return -1;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
        ret = ret.trim();
        return CommonUtil.resolveProcessID(ret, CommonUtil.PROCESS_NAME_MAIN);
    }

    private String buildMainCmd(IDevice device) {
        //app_process -Djava.class.path=/data/app/com.cxplan.mediate-1/base.apk
        ///data/local/tmp --nice-name=com.cxplan.projection.mediate com.cxplan.mediate.process.Main
        String mainPackagePath = AdbUtil.getPackagePath(CommonUtil.PACKAGE_MAIN, device);
        if (mainPackagePath == null) {//is not installed
            throw new RuntimeException("The main package is not installed: " + AdbUtil.getDeviceId(device));
        }
        StringBuilder sb = new StringBuilder("app_process -Djava.class.path=");
        sb.append(mainPackagePath).append(" /data/local/tmp --nice-name=").
                append(CommonUtil.PROCESS_NAME_MAIN).append(" com.cxplan.projection.mediate.process.Main");
        return sb.toString();
    }

    private String buildMinicapCmd(int width, int height, double scale) {
        //LD_LIBRARY_PATH=/data/local/tmp nohup /data/local/tmp/minicap -P 720x1280@360x640/0 >
        // /sdcard/cxplan/image.log 2>&1 &
        StringBuilder sb = new StringBuilder("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -S -P ");
        sb.append(height).append("x").append(width).
                append("@").append((int)(height * scale)).append("x").append((int)(width * scale)).
                append("/0 ");
        return sb.toString();
    }
    private String buildMinicapNohupCmd(int width, int height, double scale) {
        //LD_LIBRARY_PATH=/data/local/tmp nohup /data/local/tmp/minicap -P 720x1280@360x640/0 >
        // /sdcard/cxplan/image.log 2>&1 &
        StringBuilder sb = new StringBuilder("LD_LIBRARY_PATH=/data/local/tmp nohup /data/local/tmp/minicap -S -P ");
        sb.append(height).append("x").append(width).
                append("@").append((int)(height * scale)).append("x").append((int)(width * scale)).
                append("/0 > /sdcard/cxplan/image.log 2>&1 &");
        return sb.toString();
    }
}
