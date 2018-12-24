package com.cxplan.projection.core;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.adb.AdbUtil;
import com.cxplan.projection.core.adb.DeviceForward;
import com.cxplan.projection.core.adb.ForwardManager;
import com.cxplan.projection.core.connection.DeviceConnectionEvent;
import com.cxplan.projection.core.connection.DeviceConnectionListener;
import com.cxplan.projection.core.connection.DeviceReconnectionManager;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.model.DeviceInfo;
import com.cxplan.projection.net.DSCodecFactory;
import com.cxplan.projection.net.DeviceIoHandlerAdapter;
import com.cxplan.projection.service.IDeviceService;
import com.cxplan.projection.service.IInfrastructureService;
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
            Future future = instance.doAddDevice(device, false);
            if (future != null) {
                futureList.add(future);
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

    public void addDevice(IDevice device) {
        doAddDevice(device, true);
    }

    /**
     * The connection to controller.
     */
    private DeviceReconnectionManager deviceReconnectManager;
    private Map<String, DefaultDeviceConnection> deviceMap;

    private ExecutorService deviceThreadPool;
    private ExecutorService executors;

    private NioSocketConnector deviceConnector;
    protected EventListenerList deviceListenerList = new EventListenerList();

    private Application() {
        ForwardManager.getInstance();
        deviceMap = new ConcurrentHashMap<>();
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
    private Future<?> doAddDevice(final IDevice device, final boolean notifyController) {
        final String id = AdbUtil.getDeviceId(device);
        if (deviceMap.containsKey(id)) {
            return null;
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("The device (serial=" + id + ") is online");

                DefaultDeviceConnection deviceConnection;
                try {
                    deviceConnection = new DefaultDeviceConnection(id, device, Application.this);
                    deviceMap.put(id, deviceConnection);

                    if (notifyController) {
                        fireOnDeviceCreatedEvent(deviceConnection);
                    }
                } catch (Exception e) {
                    logger.error("initializing device(" + id + ") failed: " + e.getMessage(), e);
                    return;
                }

                //load setting
                Setting.getInstance().loadDeviceSetting(id);
                SystemUtil.installConfig(deviceConnection);

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
                if (deviceConnection.getScreenWidth() < 1
                        || deviceConnection.getScreenHeight() < 1) {
                    Dimension dim = AdbUtil.getPhysicalSize(device);
                    DeviceInfo di = (DeviceInfo) deviceConnection.getDeviceMeta();
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

    public void removeDevice(String id) {
        DefaultDeviceConnection pm = deviceMap.remove(id);
        logger.info("The device (serial=" + id + ") is removed");

        try {
            fireOnDeviceRemovedEvent(pm);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        //remove message forward
        try {
            DeviceForward forward = ForwardManager.getInstance().removeMessageForward(id);
            if (forward != null) {
                pm.getDevice().removeForward(forward.getLocalPort(), forward.getRemotePort());
            }
        } catch (Exception e) {
            logger.error("Removing message forward failed: " + e.getMessage(), e);
        }
        //remove image forward
        try {
            DeviceForward forward = ForwardManager.getInstance().removeImageForward(id);
            if (forward != null) {
                pm.getDevice().removeForward(forward.getLocalPort(), forward.getRemoteSocketName(),
                        IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            }
        } catch (Exception e) {
            logger.error("Removing image forward failed: " + e.getMessage(), e);
        }
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

    private void createDeviceConnector() {
        NioSocketConnector connector = new NioSocketConnector();

        connector.setHandler(new DeviceIoHandlerAdapter(this));
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter( new DSCodecFactory()));
        connector.getFilterChain().addLast("threadModel", new ExecutorFilter(executors));

        deviceConnector = connector;
    }

}
