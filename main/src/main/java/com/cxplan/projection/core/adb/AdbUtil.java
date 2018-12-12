package com.cxplan.projection.core.adb;

import com.android.ddmlib.*;
import com.cxplan.projection.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Kenny
 * created on 2018/8/20
 */
public class AdbUtil {

    private static final Logger logger = LoggerFactory.getLogger(AdbUtil.class);

    public static String getDeviceId(IDevice device) {
        if (device == null) {
            return null;
        }

        String value = device.getProperty("ro.ytx.imei");
        if (value == null) {
            value = device.getProperty("ro.serialno");
        }
        return value == null ? device.getSerialNumber() : value;
    }

    /**
     * Retrieve the path of specified package.
     * @param packageName package name.
     * @param device device object.
     * @return the package path, null value indicates the package doesn't exist.
     */
    public static String getPackagePath(String packageName, IDevice device) {
        String cmd = "pm path " + packageName;
        String ret = shell(cmd, device);
        if (StringUtil.isEmpty(ret)) {//the package doesn't exist.
            return null;
        }
        ret = ret.trim();
        int index = ret.indexOf(":");
        if (index == -1) {
            throw new RuntimeException("The illegal package path format: " + ret);
        }

        return ret.substring(index + 1);
    }

    public static String shell(String cmd, IDevice device) {
        // 5000 is the default timeout from the ddmlib.
        // This timeout arg is needed to the backwards compatibility.
        return shell(cmd, 5000, device);
    }

    /**
     * Execute a shell command, and return the output string.
     * A timeout can be specified in millisecond.
     *
     * @param cmd the shell command
     * @param timeout the max time of executing command.
     * @param device device object.
     * @return the result of command.
     */
    public static String shell(String cmd, int timeout, IDevice device) {
        CommandOutputCapture capture = new CommandOutputCapture();
        try {
            device.executeShellCommand(cmd, capture, timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return capture.toString();
    }

    /**
     * Execute a shell command which will not be terminated except for forcing
     * to exit manually.
     * This method should be invoked in another thread usually, because the thread
     * is blocked util the vm process exits.
     *
     * @param cmd the shell command
     * @param device device object.
     * @throws AdbCommandRejectedException
     * @throws IOException
     * @throws TimeoutException
     */
    public static void shellBlockingCommand(String cmd, IDevice device) throws AdbCommandRejectedException, IOException, TimeoutException {
        shellBlockingCommand(cmd, device, 2);
    }
    public static void shellBlockingCommand(String cmd, IDevice device, int timeout) throws AdbCommandRejectedException, IOException, TimeoutException {
        try {
            device.executeShellCommand(cmd, new LoggingOutputReceiver(), timeout, TimeUnit.SECONDS);
        } catch (ShellCommandUnresponsiveException e) {
            logger.info("blocking cmd: {}", e.getMessage());
        }
    }

    public static class LoggingOutputReceiver implements IShellOutputReceiver {

        public LoggingOutputReceiver() {
        }

        public void addOutput(byte[] data, int offset, int length) {
            String message = new String(data, offset, length);
            String[] arr = message.split("\n");
            for (String s : arr) {
                logger.info(s);
            }
        }

        public void flush() {
        }

        public boolean isCancelled() {
            return false;
        }
    }

}
