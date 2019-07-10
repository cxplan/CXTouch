package com.cxplan.projection.core;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.adb.AdbUtil;
import com.cxplan.projection.core.adb.ForwardManager;
import com.cxplan.projection.core.connection.DeviceConnectionEvent;
import com.cxplan.projection.core.connection.DeviceConnectionListener;
import com.cxplan.projection.core.connection.DeviceReconnectionManager;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.i18n.StringManager;
import com.cxplan.projection.i18n.StringManagerFactory;
import com.cxplan.projection.model.DeviceInfo;
import com.cxplan.projection.net.DSCodecFactory;
import com.cxplan.projection.net.DeviceIoHandlerAdapter;
import com.cxplan.projection.script.ScriptConnectionEvent;
import com.cxplan.projection.script.ScriptConnectionListener;
import com.cxplan.projection.script.io.ScriptDeviceConnection;
import com.cxplan.projection.script.io.ScriptDeviceIoHandlerAdapter;
import com.cxplan.projection.service.IDeviceService;
import com.cxplan.projection.service.IInfrastructureService;
import com.cxplan.projection.service.IScriptService;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.util.StringUtil;
import com.cxplan.projection.util.SystemUtil;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Kenny
 * created on 2018/12/5
 */
public class Application implements IApplication {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final StringManager stringMgr =
            StringManagerFactory.getStringManager(Application.class);

    private static Application instance;

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }

        return instance;
    }

    public static void loadAllDevices(AndroidDebugBridge adb) {
        final IDevice[] devices = adb.getDevices();
        if (devices.length == 0) {
            return;
        }
        Application.getInstance();//Assure that the instance of context is created.
        List<Future> futureList = new ArrayList<>(devices.length);
        for (IDevice device : devices) {
            try {
                Future future = instance.doAddDevice(device, false);
                if (future != null) {
                    futureList.add(future);
                }
            } catch (Exception ex) {
                logger.error("Loading device(" + device.getSerialNumber() + ") failed: " + ex.getMessage(), ex);
                if (AdbUtil.isWirelessDevice(device)) {
                    String wirelessWeak = stringMgr.getString("wireless.signal.weak") + device.getSerialNumber();
                    GUIUtil.showErrorMessageDialog(wirelessWeak);
                }
            }
        }

        int success = 0;
        for(Future future : futureList) {
            try {
                future.get(1, TimeUnit.MINUTES);
                success++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (success < devices.length) {
            logger.error("Loading devices failed, Success：" + instance.deviceMap.size()
                    + ", Fail：" + (devices.length - instance.deviceMap.size()));
        } else {
            logger.info("Load all devices successfully!");
        }
    }

    public synchronized void addDevice(IDevice device) {
        try {
            doAddDevice(device, true);
        } catch (Exception ex) {
            logger.error("Loading device(" + device.getSerialNumber() + ") failed: " + ex.getMessage(), ex);
            if (AdbUtil.isWirelessDevice(device)) {
                String wirelessWeak = stringMgr.getString("wireless.signal.weak") + device.getSerialNumber();
                GUIUtil.showErrorMessageDialog(wirelessWeak);
            }
        }
    }

    /**
     * The connection to controller.
     */
    private DeviceReconnectionManager deviceReconnectManager;
    private Map<String, DefaultDeviceConnection> deviceMap;
    private Map<String, ScriptDeviceConnection> scriptConnectionMap;

    private ExecutorService deviceThreadPool;
    private ExecutorService executors;

    private NioSocketConnector deviceConnector;
    private NioSocketConnector scriptDeviceConnector;
    protected EventListenerList deviceListenerList = new EventListenerList();

    //script event
    protected CopyOnWriteArrayList<ScriptConnectionListener> scriptConnectionListenerList =
            new CopyOnWriteArrayList<>();

    private Application() {
        ForwardManager.getInstance();
        deviceMap = new ConcurrentHashMap<>();
        scriptConnectionMap = new ConcurrentHashMap<>();

        loadSystemParameters();
        deviceThreadPool = Executors.newFixedThreadPool(5);
        executors = Executors.newCachedThreadPool();

        createDeviceConnector();

        deviceReconnectManager = new DeviceReconnectionManager();
        addDeviceConnectionListener(deviceReconnectManager);
        deviceReconnectManager.openMonitor();

    }

    /**
     * Initialize the connection with device, include below action:
     * 1. Check whether the mediate package is installed, meanwhile start the main process.
     * 2. Create a port forward for message channel.
     * 3. Open message channel.
     *
     * These actions are executed in another thread.
     *
     *
     * @param device device object.
     * @param notifyController flag determinate whether the device adding action should be notified controller.
     * @return Future object which control the execution thread.
     */
    private Future<?> doAddDevice(final IDevice device, final boolean notifyController) throws RuntimeException {
        final String id = AdbUtil.getDeviceId(device);
        final boolean existed = deviceMap.containsKey(id);

        DefaultDeviceConnection deviceConnection = deviceMap.get(id);
        if (deviceConnection != null) {
            boolean wirelessMode = AdbUtil.isWirelessDevice(device);
            if (wirelessMode) {
                deviceConnection.setWirelessChannel(device);
            } else {
                deviceConnection.setUsbChannel(device);
            }
        } else {
            deviceConnection = new DefaultDeviceConnection(id, device, this);
            String ip = AdbUtil.getDeviceIp(device);
            deviceConnection.setIp(ip);
            deviceMap.put(id, deviceConnection);
        }
        if (existed) {
            return null;
        }
        //create script connection.
        ScriptDeviceConnection scriptConnection = scriptConnectionMap.get(id);
        if (scriptConnection == null) {
            scriptConnection = new ScriptDeviceConnection(id, device, this);
            scriptConnectionMap.put(id, scriptConnection);
        }

        final DefaultDeviceConnection connection = deviceConnection;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("The device (serial=" + id + ") is online");

                try {
                    if (notifyController) {
                        fireOnDeviceCreatedEvent(connection);
                    }
                } catch (Exception e) {
                    logger.error("initializing device(" + id + ") failed: " + e.getMessage(), e);
                    return;
                }

                //load setting
                Setting.getInstance().loadDeviceSetting(id);
                SystemUtil.installConfig(connection);

                //clean environment( kill old image service process )
                int pid = getInfrastructureService().getMinicapProcessID(id);
                AdbUtil.killProcess(pid, device);

                //create forward by adb
                try {
                    int port = ForwardManager.getInstance().putMessageForward(id);
                    device.createForward(port, ForwardManager.MESSAGE_REMOTE_PORT);
                } catch (Exception e) {
                    logger.error("Creating forward for device(" + id + ") failed: " + e.getMessage(), e);
                    return;
                }

                //check screen size
                if (connection.getScreenWidth() < 1
                        || connection.getScreenHeight() < 1) {
                    Dimension dim = AdbUtil.getPhysicalSize(device);
                    DeviceInfo di = (DeviceInfo) connection.getDeviceMeta();
                    di.setScreenWidth(dim.width);
                    di.setScreenHeight(dim.height);
                }
                //connect to cxplan.
                /*try {
                    deviceConnection.connect(true);
                } catch (Exception e) {
                    logger.error("Connecting to device(" + id + ") failed: " + e.getMessage(), e);
                    if (deviceReconnectManager.isMonitoring()) {
                        deviceReconnectManager.addConnection(deviceConnection);
                    }
                    return;
                }*/

            }
        };
        return deviceThreadPool.submit(task);
    }

    public synchronized void removeDevice(IDevice device) {
        String id = AdbUtil.getDeviceId(device);
        DefaultDeviceConnection pm = deviceMap.get(id);
        if (pm == null) {
            return;
        }

        if (!pm.removeDeviceChannel(device)) {
            logger.info("there is no channel, the connection will be removed: {}", id);
            removeDeviceConnection(id);
        } else {
            if (!pm.hasUsbChannel()) {//Only script is supported on usb channel.
                //remove script
                ScriptDeviceConnection scriptConnection = scriptConnectionMap.remove(id);
                scriptConnection.close();
            }
        }

    }
    public synchronized void removeDeviceConnection(String id) {
        DefaultDeviceConnection pm = deviceMap.remove(id);
        if (pm == null) {
            return;
        }
        logger.info("The device (serial=" + id + ") is removed");

        try {
            fireOnDeviceRemovedEvent(pm);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            ScriptDeviceConnection scriptConnection = scriptConnectionMap.remove(id);
            scriptConnection.close();
        }

        pm.removePortForward();
    }

    @Override
    public ExecutorService getExecutors() {
        return executors;
    }

    @Override
    public IDeviceService getDeviceService() {
        IDeviceService deviceService = ServiceFactory.getService("deviceService");
        return deviceService;
    }

    @Override
    public IScriptService getScriptService() {
        IScriptService scriptService = ServiceFactory.getService("scriptService");
        return scriptService;
    }

    @Override
    public IInfrastructureService getInfrastructureService() {
        IInfrastructureService infrastructureService = ServiceFactory.getService("infrastructureService");
        return infrastructureService;
    }

    public void addDeviceConnectionListener(DeviceConnectionListener listener) {
        deviceListenerList.add(DeviceConnectionListener.class, listener);
    }
    public void removeDeviceConnectionListener(DeviceConnectionListener listener) {
        deviceListenerList.remove(DeviceConnectionListener.class, listener);
    }

    public void addScriptConnectionListener(ScriptConnectionListener listener) {
        scriptConnectionListenerList.add(listener);
    }

    public void removeScriptConnectionListener(ScriptConnectionListener listener) {
        scriptConnectionListenerList.remove(listener);
    }

    public List<String> getDeviceList() {
        return new ArrayList<>(deviceMap.keySet());
    }

    public int getDeviceSize() {
        return deviceMap.size();
    }

    @Override
    public DefaultDeviceConnection getDeviceConnection(String deviceId) {
        return deviceMap.get(deviceId);
    }

    @Override
    public ScriptDeviceConnection getScriptConnection(String deviceId) {
        return scriptConnectionMap.get(deviceId);
    }

    private void loadSystemParameters() {
//        hubId = SystemUtil.getSystemProperty("id");
    }

    @Override
    public String getDeviceName(String deviceId) {
        IDeviceConnection connection = getDeviceConnection(deviceId);
        if (connection == null) {
            return null;
        }
        if (StringUtil.isEmpty(connection.getDeviceName())) {
            return connection.getManufacturer() + " " +
                    connection.getDeviceModel();
        } else {
            return connection.getDeviceName();
        }
    }

    public NioSocketConnector getDeviceConnector() {
        return deviceConnector;
    }

    public NioSocketConnector getScriptDeviceConnector() {
        return scriptDeviceConnector;
    }

    /**
     * Fire image coming event to observers, and return the result of consuming event.
     *
     * @param deviceConnection the device connection.
     * @param image The new image object.
     * @return true: There are some observers interesting with this event.
     *         false: There is no observer interesting with this event.
     */
    public boolean fireOnDeviceImageEvent(IDeviceConnection deviceConnection, Image image) {
        DeviceConnectionEvent event = new DeviceConnectionEvent(deviceConnection, DeviceConnectionEvent.ConnectionType.IMAGE);
        event.setVideoFrame(image);
        // Guaranteed to return a non-null array
        Object[] listeners = deviceListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        int consumeCount = 0;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==DeviceConnectionListener.class) {
                try {
                    boolean ret = ((DeviceConnectionListener)listeners[i+1]).frameReady(event);
                    if (ret) {
                        consumeCount++;
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return consumeCount > 0;

    }
    public void fireOnDeviceConnectedEvent(DefaultDeviceConnection deviceConnection) {
        fireOnDeviceConnectedEvent(deviceConnection, DeviceConnectionEvent.ConnectionType.MESSAGE);
    }
    public void fireOnDeviceConnectedEvent(DefaultDeviceConnection deviceConnection, DeviceConnectionEvent.ConnectionType evenType) {
        DeviceConnectionEvent event = new DeviceConnectionEvent(deviceConnection, evenType);
        // Guaranteed to return a non-null array
        Object[] listeners = deviceListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==DeviceConnectionListener.class) {
                try {
                    ((DeviceConnectionListener) listeners[i + 1]).connected(event);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }
    protected void fireOnDeviceCreatedEvent(DefaultDeviceConnection deviceConnection) {
        DeviceConnectionEvent event = new DeviceConnectionEvent(deviceConnection, DeviceConnectionEvent.ConnectionType.MESSAGE);
        // Guaranteed to return a non-null array
        Object[] listeners = deviceListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==DeviceConnectionListener.class) {
                try {
                    ((DeviceConnectionListener) listeners[i + 1]).created(event);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }
    public void fireOnDeviceConnectionClosedEvent(DefaultDeviceConnection deviceConnection, DeviceConnectionEvent.ConnectionType type) {
        DeviceConnectionEvent event = new DeviceConnectionEvent(deviceConnection, type);
        // Guaranteed to return a non-null array
        Object[] listeners = deviceListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==DeviceConnectionListener.class) {
                try {
                    ((DeviceConnectionListener)listeners[i+1]).connectionClosed(event);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

    public void fireOnDeviceChannelChangedEvent(DefaultDeviceConnection deviceConnection) {
        DeviceConnectionEvent event = new DeviceConnectionEvent(deviceConnection, DeviceConnectionEvent.ConnectionType.ADB);
        // Guaranteed to return a non-null array
        Object[] listeners = deviceListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==DeviceConnectionListener.class) {
                try {
                    ((DeviceConnectionListener)listeners[i+1]).deviceChannelChanged(event);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

    protected void fireOnDeviceRemovedEvent(DefaultDeviceConnection deviceConnection) {
        DeviceConnectionEvent event = new DeviceConnectionEvent(deviceConnection, DeviceConnectionEvent.ConnectionType.MESSAGE);
        // Guaranteed to return a non-null array
        Object[] listeners = deviceListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==DeviceConnectionListener.class) {
                try {
                    ((DeviceConnectionListener)listeners[i+1]).removed(event);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

    public void fireOnScriptConnectedEvent(ScriptDeviceConnection scriptConnection) {
        ScriptConnectionEvent event = new ScriptConnectionEvent(scriptConnection);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (ScriptConnectionListener listener : scriptConnectionListenerList) {
            try {
                listener.connected(event);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public void fireOnScriptConnectionClosedEvent(ScriptDeviceConnection scriptConnection) {
        ScriptConnectionEvent event = new ScriptConnectionEvent(scriptConnection);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (ScriptConnectionListener listener : scriptConnectionListenerList) {
            try {
                listener.connectionClosed(event);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    private void createDeviceConnector() {
        NioSocketConnector connector = new NioSocketConnector();

        connector.setHandler(new DeviceIoHandlerAdapter(this));
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter( new DSCodecFactory()));
        connector.getFilterChain().addLast("threadModel", new ExecutorFilter(executors));

        deviceConnector = connector;

        NioSocketConnector scriptConnector = new NioSocketConnector();

        scriptConnector.setHandler(new ScriptDeviceIoHandlerAdapter(this));
        scriptConnector.getFilterChain().addLast("codec", new ProtocolCodecFilter( new DSCodecFactory()));
        scriptConnector.getFilterChain().addLast("threadModel", new ExecutorFilter(executors));

        scriptDeviceConnector = scriptConnector;


    }

}
