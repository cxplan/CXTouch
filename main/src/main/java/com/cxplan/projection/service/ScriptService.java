package com.cxplan.projection.service;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.cxplan.projection.MonkeyConstant;
import com.cxplan.projection.core.CXService;
import com.cxplan.projection.core.DefaultDeviceConnection;
import com.cxplan.projection.core.adb.AdbUtil;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.script.ViewNode;
import com.cxplan.projection.script.io.ScriptDeviceConnection;
import com.cxplan.projection.util.CommonUtil;
import com.cxplan.projection.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * @author Kenny
 * created on 2019/3/22
 */
@CXService("scriptService")
public class ScriptService extends BaseBusinessService implements IScriptService {
    private static final Logger logger = LoggerFactory.getLogger(ScriptService.class);

    private HashSet<String> startedDeviceSet = new HashSet<>();

    @Override
    public String getScriptPackageInstallPath(String deviceId) {
        DefaultDeviceConnection connection = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }
        String scriptPackagePath = AdbUtil.getPackagePath(CommonUtil.PACKAGE_SCRIPT, connection.getDevice());
        return scriptPackagePath;
    }

    @Override
    public int getScriptPackageVersion(String deviceId) {
        DefaultDeviceConnection connection = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }
        int versionCode = AdbUtil.getPackageVersionCode(CommonUtil.PACKAGE_SCRIPT, connection.getDevice());
        return versionCode;
    }

    @Override
    public void installScriptService(String deviceId) {
        DefaultDeviceConnection connection = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The device doesn't exist: " + deviceId);
        }

        try {
            connection.getDevice().installPackage("res/mediate/CXScript.apk", true);
        } catch (InstallException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean startScriptService(final IDevice device) {
        int pid = AdbUtil.checkApplicationProcess(device, CommonUtil.PROCESS_NAME_SCRIPT);
        if (pid > 0) {
            logger.info("The main process existed already: " + pid);
            return true;
        }

        final String deviceId = AdbUtil.getDeviceId(device);
        final String cmd = buildStartScriptCmd(device);
        synchronized (device) {
            if (startedDeviceSet.contains(deviceId)) {
                return false;
            }
            Thread th = new Thread("ScriptService Thread-" + deviceId) {

                @Override
                public void run() {
                    try {
                        AdbUtil.shellBlockingCommand(cmd, device, 0);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    startedDeviceSet.remove(deviceId);
                    logger.info("The script process is finished: {}", deviceId);
                }
            };
            th.start();
            startedDeviceSet.add(deviceId);
        }

        return false;
    }

    /**
     * Span component according to coordinates, return all properties of component.
     *
     * @param deviceId the device ID.
     * @param x x coordinate.
     * @param y y coordinate.
     * @throws MessageException
     */
    public ViewNode spanComponent(String deviceId, int x, int y) throws MessageException{
        ScriptDeviceConnection connection = application.getScriptConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The script connection doesn't exist: " + deviceId);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_SCRIPT_SPAN);
        message.setParameter("x", x);
        message.setParameter("y", y);

        Message retMsg = MessageUtil.request(connection, message);
        int ret = retMsg.getParameter("ret");
        if (ret == 0) {
            logger.error("There is no component found on ({},{})", x, y);
            return null;
        } else {
            String dataString = retMsg.getParameter("data");
            ViewNode viewNode = StringUtil.json2Object(dataString, ViewNode.class);

            return viewNode;
        }
    }

    /**
     * Dump the hierarchy of views on specified device.
     *
     * @param deviceId device ID.
     * @throws MessageException
     */
    public byte[] dumpHierarchy(String deviceId) throws MessageException {
        ScriptDeviceConnection connection = application.getScriptConnection(deviceId);
        if (connection == null) {
            throw new IllegalArgumentException("The script connection doesn't exist: " + deviceId);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_SCRIPT_DUMP);
        Message retMsg = MessageUtil.request(connection, message, 50000);
        byte[] data = retMsg.getParameter("data");

        return data;
    }

    @Override
    public void touchDown(String deviceId, int x, int y, int seqNum) throws MessageException {
        ScriptDeviceConnection sdc = application.getScriptConnection(deviceId);
        if (sdc == null) {
            String error = "The script connection doesn't exist: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("x", x);
        message.setParameter("y", y);
        message.setParameter("seq", seqNum);
        message.setParameter("type", MonkeyConstant.EVENT_TOUCH_DOWN);

        sendMessage(sdc, message);
    }

    @Override
    public void touchUp(String deviceId, int x, int y) throws MessageException {
        ScriptDeviceConnection sdc = application.getScriptConnection(deviceId);
        if (sdc == null) {
            String error = "The script connection doesn't exist: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("x", (float)x);
        message.setParameter("y", (float)y);
        message.setParameter("type", MonkeyConstant.EVENT_TOUCH_UP);

        sendMessage(sdc, message);
    }

    @Override
    public void touchMove(String deviceId, int x, int y) throws MessageException {
        ScriptDeviceConnection sdc = application.getScriptConnection(deviceId);
        if (sdc == null) {
            String error = "The script connection doesn't exist: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("x", (float)x);
        message.setParameter("y", (float)y);
        message.setParameter("type", MonkeyConstant.EVENT_TOUCH_MOVE);

        sendMessage(sdc, message);
    }

    @Override
    public boolean waitIdle(String deviceId, long timeout) throws MessageException {
        ScriptDeviceConnection sdc = application.getScriptConnection(deviceId);
        if (sdc == null) {
            String error = "The script connection doesn't exist: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_SCRIPT_WAIT_IDLE);
        message.setParameter("timeout", timeout);
        Message retMsg = request(sdc, message, timeout + 5000);
        byte ret = retMsg.getParameter("ret");

        return ret == 1;
    }

    @Override
    public int waitForView(String deviceId, ViewNode view, long timeout) throws MessageException {
        ScriptDeviceConnection sdc = application.getScriptConnection(deviceId);
        if (sdc == null) {
            String error = "The script connection doesn't exist: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_SCRIPT_WAIT_VIEW);
        message.setParameter("timeout", timeout);
        message.setParameter("view", StringUtil.toJSONString(view));
        Message retMsg = request(sdc, message, timeout + 5000);
        byte ret = retMsg.getParameter("ret");

        return ret;
    }

    private String buildStartScriptCmd(IDevice device) {
        //app_process -Djava.class.path="/system/framework/am.jar" /system/bin --nice-name=cxplan.cxscript com.android.commands.am.Am instrument
        // -w com.cxplan.projection.mediate.test/android.support.test.runner.AndroidJUnitRunner
        String scriptPackagePath = AdbUtil.getPackagePath(CommonUtil.PACKAGE_SCRIPT, device);
        if (scriptPackagePath == null) {//is not installed
            throw new RuntimeException("The script package is not installed: " + AdbUtil.getDeviceId(device));
        }
        StringBuilder sb = new StringBuilder("app_process -Djava.class.path=\"/system/framework/am.jar\" /system/bin --nice-name=");
        sb.append(CommonUtil.PROCESS_NAME_SCRIPT).append(" com.android.commands.am.Am instrument -w com.cxplan.projection.mediate.test/android.support.test.runner.AndroidJUnitRunner");
        return sb.toString();
    }
}
