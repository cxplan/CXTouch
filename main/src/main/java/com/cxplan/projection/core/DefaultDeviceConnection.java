package com.cxplan.projection.core;

import com.android.ddmlib.IDevice;
import com.cxplan.projection.IApplication;
import com.cxplan.projection.core.adb.ForwardManager;
import com.cxplan.projection.core.connection.*;
import com.cxplan.projection.core.image.ControllerImageSession;
import com.cxplan.projection.core.image.ImageProcessThread;
import com.cxplan.projection.core.image.ImageSessionID;
import com.cxplan.projection.core.image.ImageSessionManager;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.core.setting.SettingConstant;
import com.cxplan.projection.model.DeviceInfo;
import com.cxplan.projection.model.IDeviceMeta;
import com.cxplan.projection.net.DeviceIoHandlerAdapter;
import com.cxplan.projection.net.message.JID;
import com.cxplan.projection.net.message.Message;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.net.message.MessageUtil;
import com.cxplan.projection.service.IInfrastructureService;
import com.cxplan.projection.util.CommonUtil;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author KennyLiu
 * @created on 2018/5/5
 */
public class DefaultDeviceConnection extends ClientConnection implements IDeviceConnection {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDeviceConnection.class);

    private IApplication application;
    private DeviceInfo deviceMeta;
    private IDevice device;

    private SocketChannel imageChannel;
    private ImageProcessThread imageThread;

    private int connectCount = 0;//The total count of connecting to controller
    volatile private boolean isConnecting = false;

    public DefaultDeviceConnection(String id, IDevice device, IApplication application) {
        setId(new JID(id, JID.Type.DEVICE));
        this.application = application;
        deviceMeta = new DeviceInfo();
        deviceMeta.setId(id);
        try {
            deviceMeta.setManufacturer(device.getSystemProperty(IDevice.PROP_DEVICE_MANUFACTURER).get());
            deviceMeta.setCpu(device.getSystemProperty(IDevice.PROP_DEVICE_CPU_ABI).get());
            deviceMeta.setApiLevel(device.getSystemProperty(IDevice.PROP_BUILD_API_LEVEL).get());
            deviceMeta.setDeviceModel(device.getSystemProperty(IDevice.PROP_DEVICE_MODEL).get());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        this.device = device;
    }

    public IDevice getDevice() {
        return device;
    }

    public void setDevice(IDevice device) {
        this.device = device;
    }


    public int getMessageForwardPort() {
        return ForwardManager.getInstance().putMessageForward(getId());
    }

    public int getImageForwardPort() {
        return ForwardManager.getInstance().putImageForward(getId());
    }

    public void setImageChannel(SocketChannel imageChannel) {
        if (this.imageChannel != null) {
            try {
                this.imageChannel.close();
            } catch (IOException e) {
            }
        }
        this.imageChannel = imageChannel;
    }

    @Override
    public IDeviceMeta getDeviceMeta() {
        return deviceMeta;
    }

    @Override
    public String getId() {
        return getJId().getId();
    }

    @Override
    public boolean isImageChannelAvailable() {
        return imageChannel != null && imageChannel.isConnected();
    }

    @Override
    public String getPhone() {
        return deviceMeta.getPhone();
    }


    public void setPhone(String phone) {
        deviceMeta.setPhone(phone);
    }

    @Override
    public String getDeviceName() {
        return deviceMeta.getDeviceName();
    }

    @Override
    public int getVideoPort() {
        return deviceMeta.getVideoPort();
    }

    @Override
    public String getNetwork() {
        return deviceMeta.getNetwork();
    }

    @Override
    public boolean isNetworkAvailable() {
        return deviceMeta.isNetworkAvailable();
    }

    @Override
    public String getManufacturer() {
        return deviceMeta.getManufacturer();
    }

    @Override
    public String getCpu() {
        return deviceMeta.getCpu();
    }

    @Override
    public String getApiLevel() {
        return deviceMeta.getApiLevel();
    }

    @Override
    public String getDeviceModel() {
        return deviceMeta.getDeviceModel();
    }

    @Override
    public String getMediateVersion() {
        return deviceMeta.getMediateVersion();
    }

    @Override
    public int getMediateVersionCode() {
        return deviceMeta.getMediateVersionCode();
    }

    public void setNetworkAvailable(boolean networkAvailable) {
        deviceMeta.setNetworkAvailable(networkAvailable);
    }

    @Override
    public String getIp() {
        return deviceMeta.getIp();
    }

    public void setIp(String ip) {
        deviceMeta.setIp(ip);
    }

    public void setNetwork(String network) {
        deviceMeta.setNetwork(network);
    }

    @Override
    public int getScreenWidth() {
        return deviceMeta.getScreenWidth();
    }

    @Override
    public int getScreenHeight() {
        return deviceMeta.getScreenHeight();
    }

    @Override
    public double getZoomRate() {
        return deviceMeta.getZoomRate();
    }

    @Override
    public short getRotation() {
        return deviceMeta.getRotation();
    }

    @Override
    public boolean isOnline() {
        return isConnected();
    }

    public SocketChannel getImageChannel() {
        return imageChannel;
    }

    public void openImageChannel() {
        openImageChannel(null);
    }

    @Override
    public boolean openImageChannel(final ConnectStatusListener listener) {
        ImageSessionManager.getInstance().addImageSession(getId(), new ControllerImageSession(getId()));

        if (imageChannel != null && imageChannel.isConnected()) {
            if (imageThread != null && imageThread.isAlive()) {
                if (listener != null) {
                    listener.OnSuccess(this);
                }
                return true;
            }
        }
        //execute connecting by pool thread.
        Runnable task = new Runnable() {
            @Override
            public void run() {
                connectToImageService(listener);
            }
        };
        application.getExecutors().submit(task);
        return false;
    }

    public Thread getImageProcessThread() {
        return imageThread;
    }

    @Override
    public void setDeviceName(String name) {
        deviceMeta.setDeviceName(name);
    }

    @Override
    public void setZoomRate(float zoomRate) {
        deviceMeta.setZoomRate(zoomRate);
    }

    @Override
    public void setRotation(short rotation) {
        deviceMeta.setRotation(rotation);
    }

    @Override
    public void dispose() {
        closeNetworkResource();
        IDeviceConnection connection = application.getDeviceConnection(getId());
        if (connection == this) {
            Application.getInstance().removeDevice(getId());
        }
    }

    public int getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    public boolean visitMessageCollectors(Message message) {
        boolean ret = false;
        for (MessageCollector collector : getPacketCollectors()) {
            if (processMessage(collector, message)) {
                ret = true;
            }
        }

        return ret;
    }

    @Override
    public void close() {
        super.close();
        closeImageChannel();
        Application.getInstance().fireOnDeviceConnectionClosedEvent(this,
                DeviceConnectionEvent.ConnectionType.MESSAGE);
    }

    @Override
    public void connect() {
        try {
            connect(true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Connect to message service run in device, and initialize the context of connection.
     * If 'wait' is true value, the thread will be blocked util the initialization action is completed.
     * Otherwise return directly after the network connection is finished, the initialization action
     * will be executed asynchronously.
     *
     * @param wait true: wait util the initialization action is completed.
     * @throws Exception
     */
    public void connect(boolean wait) throws Exception {

        if (isConnecting) {
            return;
        }
        isConnecting = true;

        try {
            //start cxplan
            IInfrastructureService infrastructureService = application.getInfrastructureService();
            infrastructureService.startMainProcess(device);

            Thread.sleep(1000);

            int forwardPort = getMessageForwardPort();
            //ensure the forward available.
            device.createForward(forwardPort, ForwardManager.MESSAGE_REMOTE_PORT);
            //connect to forward port.
            NioSocketConnector connector = Application.getInstance().getDeviceConnector();
            ConnectFuture connFuture = connector.connect(new InetSocketAddress("localhost", forwardPort));
            connFuture.awaitUninterruptibly();

            while (!connFuture.isDone() && !connFuture.isConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            if (!connFuture.isConnected()) {
                connFuture.cancel();
                String errorMsg = "Connecting to cxplan server is timeout: forward port=" + forwardPort;
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            messageSession = connFuture.getSession();
            if (messageSession.isConnected()) {
                messageSession.setAttribute(DeviceIoHandlerAdapter.CLIENT_SESSION, this);
                messageSession.setAttribute(DeviceIoHandlerAdapter.CLIENT_ID, getJId());
                logger.info("Connect to device({}) on port({}) successfully!", getId(), forwardPort);
            } else {
                logger.error("Connecting to device({}) on port({}) failed", getId(), forwardPort);
                return;
            }

            Message createMsg = new Message(MessageUtil.CMD_DEVICE_CREATE_SESSION);
            int imageQuality = Setting.getInstance().getIntProperty(getId(),
                    SettingConstant.KEY_DEVICE_IMAGE_QUALITY, SettingConstant.DEFAULT_IMAGE_QUALITY);
            float zoomRate = Setting.getInstance().getFloatProperty(getId(),
                    SettingConstant.KEY_DEVICE_IMAGE_ZOOM_RATE, SettingConstant.DEFAULT_ZOOM_RATE);
            createMsg.setParameter("iq", imageQuality);
            createMsg.setParameter("zr", zoomRate);
            try {
                if (wait) {
                    Message retMsg = MessageUtil.request(this, createMsg, 5000);
                    prepareConnection(retMsg);
                } else {
                    MessageUtil.sendMessage(this, createMsg);
                }
            } catch (MessageException e) {
                if (messageSession != null) {
                    messageSession.closeNow();
                }
                throw e;
            }
        } finally {
            isConnecting = false;
            connectCount++;
        }
    }

    public void prepareConnection(Message message) throws MessageException {
        //1. read device information.
        String id = message.getParameter("id");
        if (!id.equals(getId())) {
            throw new RuntimeException("Preparing session failed: the id is not matched: " + id);
        }
        String phone = message.getParameter("phone");
        String imageServer = message.getParameter("host");
        Integer screenWidth = message.getParameter("sw");
        Integer screenHeight = message.getParameter("sh");
        short rotation = message.getParameter("ro");

        //app version
        String mediateVersion = message.getParameter("mediateVersion");
        Integer mediateVersionCode = message.getParameter("mediateVersionCode");

        if (imageServer == null) {
            logger.warn("The image server information is missed: [host="
                    + imageServer + "]");
        }
        DefaultDeviceConnection cd = (DefaultDeviceConnection)application.getDeviceConnection(getId());
        if (cd == null) {
            logger.error("设备未通过USB连接：id=" + getId());
            Message errorMsg = Message.createResultMessage(message);
            errorMsg.setParameter("errorType", 1);
            errorMsg.setError("设备未通过USB连接：id=" + getId());
            sendMessage(errorMsg);
            return;
        }

        cd.setPhone(phone);
        cd.deviceMeta.setIp(imageServer);

        cd.deviceMeta.setScreenWidth(screenWidth);
        cd.deviceMeta.setScreenHeight(screenHeight);
        cd.setRotation(rotation);

        //install app version information.
        cd.deviceMeta.setMediateVersion(mediateVersion);
        if (mediateVersionCode != null) {
            cd.deviceMeta.setMediateVersionCode(mediateVersionCode);
        }

        logger.info("initialize session for phone({}) successfully!", getId());

        messageSession.getConfig().setIdleTime(IdleStatus.READER_IDLE, 15);
        Application.getInstance().fireOnDeviceConnectedEvent(cd);
        logger.info("fire connected event is finished({})", getId());

    }

    @Override
    public void closeImageChannel() {
        if (!isImageChannelAvailable()) {
            return;
        }
        ImageSessionManager.getInstance().removeImageSession(getId(), new ImageSessionID(ImageSessionID.TYPE_HUB, getId()));

        if (imageChannel != null && imageChannel.isConnected()) {
            try {
                imageChannel.close();
            } catch (IOException e) {
            }
            if (imageThread != null && imageThread.isAlive()) {
                imageThread.stopMonitor();
            }

            imageThread = null;
            imageChannel = null;
        }

        Application.getInstance().fireOnDeviceConnectionClosedEvent(this,
                DeviceConnectionEvent.ConnectionType.IMAGE);
    }

    volatile boolean isConnectingImageServer = false;
    /**
     * Connect to image server, and then start image stream thread.
     * @throws MessageException
     */
    public synchronized void connectToImageService(ConnectStatusListener listener) {
        if (isImageChannelAvailable()) {
            logger.info("The image channel is ok, has no use for this operation");
            return;
        }
        if (isConnectingImageServer) {
            return;
        }
        isConnectingImageServer = true;
        try {
            //start minicap service
            IInfrastructureService infrastructureService = ServiceFactory.getService("infrastructureService");
            //check installation
            if (!infrastructureService.checkMinicapInstallation(getId())) {
                logger.warn("The minicap is not installed: " + getId());
                infrastructureService.installMinicap(getId());
            }
            //start minicap service
            infrastructureService.startMinicapService(getId());
            device.createForward(getImageForwardPort(), ForwardManager.IMAGE_REMOTE_SOCKET_NAME,
                    IDevice.DeviceUnixSocketNamespace.ABSTRACT);

            //set up ADB inputer as default input method.
            //TODO is necessary, a restore action is needed also.
            checkInputerInstallation();

            if (imageChannel != null) {
                try {
                    imageChannel.close();
                } catch (Exception e){}
                imageChannel = null;
            }

            SocketChannel imageSocketChannel = SocketChannel.open();
            SocketAddress sa = new InetSocketAddress("localhost", getImageForwardPort());
            logger.info("connecting to image server[localhost:{}]", getImageForwardPort());
            imageSocketChannel.connect(sa);

            setImageChannel(imageSocketChannel);
            logger.info("Connect to image server successfully!");

            if (listener != null) {
                try {
                    listener.OnSuccess(this);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (listener != null) {
                listener.onFailed(this, e.getMessage());
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            isConnectingImageServer = false;
        }

        if (imageThread != null) {
            imageThread.stopMonitor();
        }
        imageThread = new ImageProcessThread(this, application);
        imageThread.startMonitor();
        //fire connected event of image channel.
        Application.getInstance().fireOnDeviceConnectedEvent(this,
                DeviceConnectionEvent.ConnectionType.IMAGE);
    }

    private void checkInputerInstallation() {
        IInfrastructureService infrastructureService = ServiceFactory.getService("infrastructureService");
        String builtInputer = CommonUtil.TOUCH_INPUTER;
        //check inputer installation
        infrastructureService.switchInputer(getId(), builtInputer);
    }
}
