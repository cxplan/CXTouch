package com.cxplan.projection;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.CXAdbHelper;
import com.android.ddmlib.IDevice;
import com.cxplan.projection.core.Application;
import com.cxplan.projection.core.ServiceFactory;
import com.cxplan.projection.core.adb.AdbUtil;
import com.cxplan.projection.core.command.CommandHandlerFactory;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.core.setting.ConfigChangedListener;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.core.setting.SettingConstant;
import com.cxplan.projection.core.setting.SettingEvent;
import com.cxplan.projection.ui.laf.CXLookAndFeel;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.util.SystemUtil;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @author Kenny
 * created on 2018/12/4
 */
public class Launcher {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        String configPath = SystemUtil.CONFIG_PATH;
        //1. log configuration 100
        InputStream logFile = Launcher.class.getResourceAsStream("/log.properties");
        try {
            LogManager.getLogManager().readConfiguration(logFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String log4jConfig = configPath + "/log4j.properties";
        System.out.println("log4j config: " + log4jConfig);
        PropertyConfigurator.configure(log4jConfig);
        java.util.logging.Logger.getGlobal().setLevel(Level.WARNING);

        Setting.getInstance().addPropertyChangeListener(new DeviceSettingListener());//load system setting.
        GUIUtil.lastSelectedDir = Setting.getInstance().getProperty(SettingConstant.KEY_LAST_SELECTED_DIR, null);

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    CXLookAndFeel.install();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        //2. load service
        Application context = Application.getInstance();
        ServiceFactory.initialize(context, "com.cxplan.projection.service");
        CommandHandlerFactory.loadHandler(context, "com.cxplan.projection.command");

        //3. start ADB service
        startADB();
        System.out.println("Loading all devices is finished");

        //main frame
        MainFrame frame = new MainFrame(context);
        GUIUtil.mainFrame = frame;
        GUIUtil.centerFrameToFrame(null, frame);
        frame.setVisible(true);
    }

    private static void startADB() {
        //start ADB service
        String adbLocation = System.getenv("ANDROID_HOME");
        if (adbLocation != null) {
            adbLocation += File.separator + "platform-tools" + File.separator + "adb";
        } else {
            File builtADB = new File("resource/adb");
            if (builtADB.isDirectory() && builtADB.exists()) {
                adbLocation = "resource/adb/adb";
            } else {
                adbLocation = "adb";
            }
        }

        AndroidDebugBridge.init(false);
        final AndroidDebugBridge adb = AndroidDebugBridge.createBridge(adbLocation, false);
        try {
            logger.info("wait 1 seconds!");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        logger.info("The connection state of ADB: {}", adb.isConnected());
        if (adb.isConnected()) {
            logger.info("The number of devices: {}", adb.getDevices().length);
        } else {
            logger.info("Starting adb service failed");
            return;
        }

        //remove all forward
        try {
            CXAdbHelper.removeAllForward();
        } catch (Exception e) {
            logger.error("Removing all forward failed: " + e.getMessage(), e);
            return;
        }

        //read all devices
        Application.loadAllDevices(adb);

        //listen to changes on adb.
        AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {
            public void deviceConnected(IDevice device) {
                if (device.isOnline()) {
                    String id = AdbUtil.getDeviceId(device);
                    logger.info("The device (serial=" + id + ") is added");
                    Application.getInstance().addDevice(device);
                }
            }

            public void deviceDisconnected(IDevice device) {
                String deviceId = AdbUtil.getDeviceId(device);
                logger.info("The device (serial=" + deviceId + ") is removed");
                Application.getInstance().removeDevice(deviceId);
            }

            public void deviceChanged(IDevice device, int changeMask) {
                if (changeMask == IDevice.CHANGE_STATE && device.isOnline()) {
                    String id = AdbUtil.getDeviceId(device);
                    logger.info("The device (serial=" + id + ") is added");
                    Application.getInstance().addDevice(device);
                }
            }
        });

    }

    private static class DeviceSettingListener implements ConfigChangedListener {

        @Override
        public void changed(SettingEvent event) {
            if (event.isSystemSetting() || event.isResult()) {
                return;
            }

            if (event.getPropertyName().equals(SettingConstant.KEY_DEVICE_NAME)) {
               IDeviceConnection connection =  Application.getInstance().getDeviceConnection( event.getSource());
               if (connection == null) {
                   return;
               }

               connection.setDeviceName((String) event.getNewValue());
            } else if (event.getPropertyName().equals(SettingConstant.KEY_DEVICE_IMAGE_ZOOM_RATE)) {
                IDeviceConnection connection =  Application.getInstance().getDeviceConnection( event.getSource());
                if (connection == null) {
                    return;
                }

                connection.setZoomRate((float) event.getNewValue());
            }
        }
    }
}
