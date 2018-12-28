package com.cxplan.projection.service;

import com.android.ddmlib.IDevice;
import com.cxplan.projection.MonkeyConstant;
import com.cxplan.projection.core.CXService;
import com.cxplan.projection.core.DefaultDeviceConnection;
import com.cxplan.projection.core.adb.AdbUtil;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.util.CommonUtil;
import com.cxplan.projection.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created on 2018/6/9.
 *
 * @author kenny
 */
@CXService("deviceService")
public class DefaultDeviceService extends BaseBusinessService implements IDeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDeviceService.class);

    public DefaultDeviceService() {
    }

    @Override
    public boolean installPackage(List<File> fileList, String... deviceId) {
        if (fileList == null || fileList.size() == 0) {
            logger.error("There is no apk file found");
            return false;
        }
        if (deviceId.length > 1) {
            throw new MonkeyException("Only one phone is accepted");
        }
        logger.info("install apk for phone({}:{})", new Object[]{deviceId[0], fileList.toString()});
        String pid = deviceId[0];
        IDeviceConnection pm = application.getDeviceConnection(pid);
        if (pm == null) {
            logger.error("The phone is offline: " + deviceId);
            return false;
        }
        if(!(pm instanceof DefaultDeviceConnection)) {
            logger.error("The device connection class is not expected: " + pm.getClass().getName());
            return false;
        }

        try {
            for (File file : fileList) {

                ((DefaultDeviceConnection)pm).getDevice().installPackage(file.getAbsolutePath(), true, "-g");
                logger.info("install for single phone({}) apk:{}", deviceId[0], file.getAbsolutePath());
            }
            logger.info("install apk for phone({}) successfully", deviceId[0]);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new MonkeyException(e);
        }
    }

    @Override
    public void pullFile(String deviceId, String remote, String local) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            logger.error("The device is offline: " + deviceId);
            return;
        }

        try {
            pm.getDevice().pullFile(remote, local);
        } catch (Exception e) {
            throw new MonkeyException(e);
        }
    }

    @Override
    public int syncContact(String deviceId) throws MessageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String syncMessage(String deviceId, Date startTime, Date endTime) throws MessageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String shell(String deviceId, String cmd) {
        return shell(deviceId, cmd, 5000);
    }

    @Override
    public String shell(String deviceId, String cmd, int timeout) {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            logger.error("The device is offline: " + deviceId);
            return null;
        }

        return AdbUtil.shell(cmd, timeout, pm.getDevice());
    }

    @Override
    public void locateDevice(String deviceId) {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The device is offline: " + deviceId;
            throw new RuntimeException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_LOCATE);
        message.setParameter("pid", deviceId);

        try {
            sendMessage(pm, message);
        } catch (MessageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Image takeScreenshot(String deviceId, float zoomRate, int quality) {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The device is offline: " + deviceId;
            throw new RuntimeException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_IMAGE);
        message.setParameter("type", (short)4);
        message.setParameter("zr", zoomRate);
        message.setParameter("q", quality);

        Point size = CommonUtil.getDeviceDisplaySize(pm, zoomRate);
        int realWidth = size.x;
        int realHeight = size.y;

        Message retMsg;
        try {
            retMsg = request(pm, message);
        } catch (MessageException e) {
            logger.error(e.getMessage(), e);
            return buildEmptyImage(realWidth, realHeight);
        }

        byte[] data = retMsg.getParameter("img");
        Image bufferedImage = ImageUtil.readImage(data);

        return bufferedImage.getScaledInstance(realWidth, realHeight, Image.SCALE_SMOOTH);
    }

    private BufferedImage buildEmptyImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        return image;
    }

    private IDevice getDevice(String deviceId) {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The device is offline: " + deviceId;
            throw new MonkeyException(error);
        }

        return pm.getDevice();
    }
    @Override
    public void toggleScreenOnOff(String deviceId) throws MessageException {
        press(deviceId, MonkeyConstant.KEYCODE_POWER);
    }

    @Override
    public void home(String deviceId) throws MessageException {
        press(deviceId, MonkeyConstant.KEYCODE_HOME);
    }

    @Override
    public void reboot(String deviceId) {
        reboot(deviceId, "");
    }

    @Override
    public void startApp(String deviceId) throws MessageException {
        startActivity(deviceId,null, null,null,null,new ArrayList<String>(),new HashMap<String, Object>(),
                "com.tencent.mm/com.tencent.mm.ui.LauncherUI", 0);
    }

    /**
     * type some text to device.
     *
     */
    @Override
    public void type(String deviceId, String text) {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("s", text);
        message.setParameter("type", MonkeyConstant.EVENT_TYPE);

        try {
            sendMessage(pm, message);
        } catch (MessageException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void touch(String deviceId, int x, int y) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("x", (float)x);
        message.setParameter("y", (float)y);
        message.setParameter("type", MonkeyConstant.EVENT_TOUCH);

        pm.sendMessage(message);
    }

    @Override
    public void touchDown(String deviceId, int x, int y) throws MessageException {
        /*IChimpDevice chimpDevice = getDevice(deviceId);
        try {
            chimpDevice.getManager().touchDown(x, y);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }*/
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("x", (float)x);
        message.setParameter("y", (float)y);
        message.setParameter("type", MonkeyConstant.EVENT_TOUCH_DOWN);

        sendMessage(pm, message);
    }

    @Override
    public void touchUp(String deviceId, int x, int y) throws MessageException {
        /*IChimpDevice chimpDevice = getDevice(deviceId);
        try {
            chimpDevice.getManager().touchUp(x, y);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }*/
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("x", (float)x);
        message.setParameter("y", (float)y);
        message.setParameter("type", MonkeyConstant.EVENT_TOUCH_UP);

        sendMessage(pm, message);
    }

    @Override
    public void touchMove(String deviceId, int x, int y) throws MessageException {
        /*IChimpDevice chimpDevice = getDevice(deviceId);
        try {
            chimpDevice.getManager().touchMove(x, y);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }*/
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }
        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("x", (float)x);
        message.setParameter("y", (float)y);
        message.setParameter("type", MonkeyConstant.EVENT_TOUCH_MOVE);

        sendMessage(pm, message);
    }

    /**
     * Press a physical button on the device.
     *
     */
    @Override
    public void press(String deviceId, int keyCode) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("kc", keyCode);
        message.setParameter("type", MonkeyConstant.EVENT_PRESS);

        pm.sendMessage(message);
    }

    @Override
    public void keyDown(String deviceId, String name) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("ch", name);
        message.setParameter("type", MonkeyConstant.EVENT_KEY_DOWN);

        pm.sendMessage(message);
    }

    @Override
    public void keyUp(String deviceId, String name) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("ch", name);
        message.setParameter("type", MonkeyConstant.EVENT_KEY_UP);

        pm.sendMessage(message);
    }

    @Override
    public void reboot(String deviceId, String into) {
        IDevice device = getDevice(deviceId);
        try {
            device.reboot(into);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void back(String deviceId) throws MessageException {
        press(deviceId, MonkeyConstant.KEYCODE_BACK);
    }

    @Override
    public void wake(String deviceId) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("type", MonkeyConstant.EVENT_WAKE);

        pm.sendMessage(message);
    }

    @Override
    public void startActivity(String deviceId, String uri, String action, String data, String mimeType, Collection<String> categories, Map<String, Object> extras, String component, int flags) throws MessageException {
        List<String> intentArgs = buildIntentArgString(uri, action, data, mimeType, categories,
                extras, component, flags);
        List<String> cmdList = new ArrayList<>(intentArgs.size() + 2);
        cmdList.add("am");
        cmdList.add("start");
        cmdList.addAll(intentArgs);
        shell(deviceId, cmdList.toArray(ZERO_LENGTH_STRING_ARRAY));
    }

    @Override
    public void scrollUp(String deviceId) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("vs", 0.8f);
        message.setParameter("x", 0f);
        message.setParameter("y", (float)pm.getScreenHeight()/2);
        message.setParameter("type", MonkeyConstant.EVENT_SCROLL);

        pm.sendMessage(message);
    }

    @Override
    public void scrollDown(String deviceId) throws MessageException {
        DefaultDeviceConnection pm = (DefaultDeviceConnection)application.getDeviceConnection(deviceId);
        if (pm == null) {
            String error = "The phone is offline: " + deviceId;
            logger.error(error);
            throw new MonkeyException(error);
        }

        Message message = new Message(MessageUtil.CMD_DEVICE_MONKEY);
        message.setParameter("vs", -0.8f);
        message.setParameter("x", 0f);
        message.setParameter("y", (float)pm.getScreenHeight()/2);
        message.setParameter("type", MonkeyConstant.EVENT_SCROLL);

        pm.sendMessage(message);
    }

    private String shell(String deviceId, String... args) {
        StringBuilder cmd = new StringBuilder();
        for (String arg : args) {
            cmd.append(arg).append(" ");
        }
        return shell(deviceId, cmd.toString());
    }

    private static boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }

    private List<String> buildIntentArgString(String uri, String action, String data, String mimetype,
                                              Collection<String> categories, Map<String, Object> extras, String component,
                                              int flags) {
        List<String> parts = new ArrayList<>();
        // from adb docs:
        //<INTENT> specifications include these flags:
        //    [-a <ACTION>] [-d <DATA_URI>] [-t <MIME_TYPE>]
        //    [-c <CATEGORY> [-c <CATEGORY>] ...]
        //    [-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...]
        //    [--esn <EXTRA_KEY> ...]
        //    [--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...]
        //    [-e|--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...]
        //    [-n <COMPONENT>] [-f <FLAGS>]
        //    [<URI>]
        if (!isNullOrEmpty(action)) {
            parts.add("-a");
            parts.add(action);
        }
        if (!isNullOrEmpty(data)) {
            parts.add("-d");
            parts.add(data);
        }
        if (!isNullOrEmpty(mimetype)) {
            parts.add("-t");
            parts.add(mimetype);
        }
        // Handle categories
        for (String category : categories) {
            parts.add("-c");
            parts.add(category);
        }
        // Handle extras
        for (Map.Entry<String, Object> entry : extras.entrySet()) {
            // Extras are either boolean, string, or int.  See which we have
            Object value = entry.getValue();
            String valueString;
            String arg;
            if (value instanceof Integer) {
                valueString = Integer.toString((Integer) value);
                arg = "--ei";
            } else if (value instanceof Boolean) {
                valueString = Boolean.toString((Boolean) value);
                arg = "--ez";
            } else {
                // treat is as a string.
                valueString = value.toString();
                arg = "--es";
            }
            parts.add(arg);
            parts.add(entry.getKey());
            parts.add(valueString);
        }
        if (!isNullOrEmpty(component)) {
            parts.add("-n");
            parts.add(component);
        }
        if (flags != 0) {
            parts.add("-f");
            parts.add(Integer.toString(flags));
        }
        if (!isNullOrEmpty(uri)) {
            parts.add(uri);
        }
        return parts;
    }

    private static final String[] ZERO_LENGTH_STRING_ARRAY = new String[0];
}
