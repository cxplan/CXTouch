package com.cxplan.projection.ui;

import com.cxplan.projection.IApplication;
import com.cxplan.projection.MonkeyConstant;
import com.cxplan.projection.core.connection.ConnectStatusListener;
import com.cxplan.projection.core.connection.DeviceConnectionEvent;
import com.cxplan.projection.core.connection.DeviceConnectionListener;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.core.setting.SettingConstant;
import com.cxplan.projection.i18n.StringManager;
import com.cxplan.projection.i18n.StringManagerFactory;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.service.IDeviceService;
import com.cxplan.projection.ui.component.BaseFrame;
import com.cxplan.projection.ui.component.monkey.MonkeyInputListener;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.ui.util.IconUtil;
import com.cxplan.projection.util.ImageUtil;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.JideButton;
import info.clearthought.layout.TableLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on 2017/4/7.
 *
 * @author kenny
 */
public class DeviceImageFrame extends BaseFrame {
    private static final StringManager stringMgr =
            StringManagerFactory.getStringManager(DeviceImageFrame.class);

    private static final Logger logger = LoggerFactory.getLogger(DeviceImageFrame.class);

    public static final ImageIcon DISCONNECT = IconUtil.getIcon("/resource/image/disconnected.png");
    public static final ImageIcon WAITING = IconUtil.getIcon("/image/wait.gif");
    public static final ImageIcon ICON_BACK = IconUtil.getIcon("/image/monkey/back.png");
    public static final ImageIcon ICON_HOME = IconUtil.getIcon("/image/monkey/home.png");
    public static final ImageIcon ICON_POWER = IconUtil.getIcon("/image/monkey/power.png");
    public static final ImageIcon ICON_WEIXIN = IconUtil.getIcon("/image/monkey/wx.png");

    private static Map<String, DeviceImageFrame> instanceMap = new HashMap<>();

    /**
     * Get a instance of projection window for specified device. If the instance exists already,
     * return it directly. When instance doesn't exist, if parameter 'create' to true, a new instance
     * will be created, otherwise a null value will be returned.
     *
     * @param deviceId the device ID
     * @param application application context.
     * @param create a flag indicates whether a new instance of projection window should
     *               be created if instance doesn't exist.
     * @return the instance of projection window, a null value will be returned if the instance
     *         doesn't exist and parameter 'create' is false.
     */
    public static DeviceImageFrame getInstance(String deviceId, IApplication application, boolean create) {
        DeviceImageFrame instance = instanceMap.get(deviceId);
        if (instance == null && create) {
            IDeviceConnection connection = application.getDeviceConnection(deviceId);
            if (connection == null) {
                throw new RuntimeException("The device doesn't exist: " + deviceId);
            }
            instance = new DeviceImageFrame(connection, application);
            instanceMap.put(deviceId, instance);
        }

        return instance;
    }

    private DeviceDisplayPanel clientScreen;
    private DefaultOverlayable screenComp;
    private JLabel tipLabel;

    private IApplication application;
    private IDeviceConnection connection;
    private IDeviceService monkeyService;

    private DeviceConnectionListener deviceConnectionListener;
    private LinkedBlockingQueue<Object> imageQueue = new LinkedBlockingQueue<>(100);
    private ShowImageThread imageThread;

    //Indicate whether there is no frame received since the image channel is connected.
    private boolean isFirstFrame = true;

    private DeviceImageFrame(IDeviceConnection connection, IApplication application ) {
        super("The device frame: " + application.getDeviceName(connection.getId()) + "(" + connection.getId() + ")");
        if (GUIUtil.mainFrame != null) {
            setIconImage(GUIUtil.mainFrame.getIconImage());
        }
        instanceMap.put(connection.getId(), this);
        this.application = application;
        this.connection = connection;
        monkeyService = application.getDeviceService();

        initView();
        installListener();

        setSize(430, 700);

        boolean alwaysTop = Setting.getInstance().getBooleanProperty(connection.getId(),
                SettingConstant.KEY_DEVICE_ALWAYS_TOP, false);
        setAlwaysOnTop(alwaysTop);
    }

    /**
     * Show screen mapping window instead of using method setVisible(boolean).
     */
    public void showWindow() {
        if (isVisible()) {
            setVisible(true);
            return;
        }

        GUIUtil.centerFrameToFrame(null, this);
        setVisible(true);
        application.removeDeviceConnectionListener(deviceConnectionListener);
        application.addDeviceConnectionListener(deviceConnectionListener);
        if (imageThread == null || !imageThread.isAlive()) {
            imageThread = new ShowImageThread();
            imageThread.start();
        }

        openScreenProjection();
    }

    @Override
    public void dispose() {
        connection.closeImageChannel();
        application.removeDeviceConnectionListener(deviceConnectionListener);
        super.dispose();
        if (imageThread != null) {
            imageThread.stopShow();
        }
        instanceMap.remove(connection.getId());
    }

    private void openScreenProjection() {
        if (connection.isConnected()) {
            openImageChannel();
        } else {
            connection.connect();
        }
    }

    public void openImageChannel() {
        if (!connection.isImageChannelAvailable()) {
            showWaitingTip(stringMgr.getString("status.connecting"));
            boolean ret = connection.openImageChannel(new ConnectStatusListener() {
                @Override
                public void OnSuccess(IDeviceConnection connection) {
                }

                @Override
                public void onFailed(IDeviceConnection connection, String error) {
                    GUIUtil.showErrorMessageDialog(error, stringMgr.getString("connect.fail"));
                }
            });
            if (ret) {
                showWaitingTip(stringMgr.getString("status.waitimage"));
                takeScreenshot();
            }
        } else {
            showWaitingTip(stringMgr.getString("status.waitimage"));
            takeScreenshot();
        }
    }

    private void initView() {
        double b=10;
        double[][] size=new double[][]{{b,400,TableLayout.FILL,b},
                {b,
                        TableLayout.FILL,b}};
        JPanel mainContent = (JPanel)getContentPane();
        mainContent.setLayout(new TableLayout((size)));

        clientScreen = new DeviceDisplayPanel(getGraphicsConfiguration(), monkeyInputListener);
        clientScreen.setBackground(Color.gray);
        clientScreen.setDeviceZoomRate(connection.getZoomRate());
        clientScreen.setExtComponent(createDeviceButtonPanel());
        clientScreen.getCanvas().setVisible(false);
        screenComp = new DefaultOverlayable(clientScreen);
        screenComp.setBackground(clientScreen.getBackground());
        tipLabel = new JLabel();
        screenComp.addOverlayComponent(tipLabel,
                SwingConstants.CENTER);
        screenComp.setOverlayVisible(true);
        mainContent.add(screenComp, "1, 1");

    }

    private void installListener() {
        deviceConnectionListener = new DeviceConnectionListener() {
            @Override
            public boolean frameReady(DeviceConnectionEvent event) {
                if(!checkConnection(event)) {
                    return false;
                }

                if(!isFirstFrame && !DeviceImageFrame.this.isFocused()) {
                    return true;
                } else if (isFirstFrame) {
                    showScreenResult();
                }

                isFirstFrame = false;
                Object frame = event.getVideoFrame();
                imageQueue.offer(frame);
                return true;
            }

            @Override
            public void created(DeviceConnectionEvent event) {
                if(!checkConnection(event)) {
                    return;
                }

                if (event.getType() != DeviceConnectionEvent.ConnectionType.NETWORK) {
                    showWaitingTip("", DISCONNECT);
                }
            }

            @Override
            public void removed(DeviceConnectionEvent event) {
                disconnect(event);
            }

            @Override
            public void connected(DeviceConnectionEvent event) {
                if(!checkConnection(event)) {
                    return;
                }

                if (event.getType() == DeviceConnectionEvent.ConnectionType.MESSAGE) {
                    openImageChannel();
                } else if (event.getType() == DeviceConnectionEvent.ConnectionType.IMAGE) {
                    showWaitingTip(stringMgr.getString("status.waitimage"));
                    takeScreenshot();
                }
            }

            @Override
            public void connectionClosed(DeviceConnectionEvent event) {
                disconnect(event);
            }

            @Override
            public void connectionClosedOnError(DeviceConnectionEvent event, Exception e) {
                disconnect(event);
            }

            private void disconnect(DeviceConnectionEvent event) {
                if (DeviceImageFrame.this.connection != event.getSource()) {
                    return;
                }
                if (event.getType() != DeviceConnectionEvent.ConnectionType.NETWORK) {
                    showWaitingTip("", DISCONNECT);
                }
            }

            /**
             * Revise the current connection by specified connection event,
             * the device ID is considered as key information, not reference to object.
             *
             * @param event device connection event.
             * @return true: event is matched with current connection, false: not matched.
             */
            private boolean checkConnection(DeviceConnectionEvent event) {
                IDeviceConnection currentConnection = DeviceImageFrame.this.connection;
                if (currentConnection == null) {
                    return false;
                }
                if (currentConnection != event.getSource()) {
                    if (currentConnection.getId().equals(event.getSource().getId())) {
                        DeviceImageFrame.this.connection = event.getSource();
                    } else {
                        return false;
                    }
                }

                return true;
            }
        };

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void showScreenResult() {
        isFirstFrame = true;
        showMonkeyScreen();
        if (DeviceImageFrame.this.connection.isImageChannelAvailable() &&
                (imageThread == null || !imageThread.isAlive())) {
            imageThread = new ShowImageThread();
            imageThread.start();
        }
    }

    private void takeScreenshot() {
        double rate = connection.getZoomRate();
        int realWidth = (int)(connection.getScreenWidth() * rate);
        int realHeight = (int)(connection.getScreenHeight() * rate);
        Image image;
        try {
            image = monkeyService.takeScreenshot(connection.getId(), realHeight, realWidth);
        } catch (MessageException e) {
            logger.error(e.getMessage(), e);
            GUIUtil.showErrorMessageDialog(e.getMessage());
            return;
        }

        isFirstFrame = false;
        showScreenResult();
        imageQueue.offer(image);
    }

    private JPanel createDeviceButtonPanel() {
        int gap = 40;
        JPanel panel = new JPanel();
        panel.setBackground(Color.GRAY);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JideButton backBtn = new JideButton(ICON_BACK);
        backBtn.setButtonStyle(JideButton.HYPERLINK_STYLE);
        backBtn.setToolTipText(stringMgr.getString("navi.button.back.tooltip"));
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!connection.isOnline()) {
                    GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
                    return;
                }
                try {
                    monkeyService.press(connection.getId(), MonkeyConstant.KEYCODE_BACK);
                } catch (MessageException e1) {
                    logger.error("Pressing back button failed:" + e1.getMessage(), e1);
                }
            }
        });
        panel.add(backBtn);

        Component box = Box.createHorizontalStrut(gap);
        panel.add(box);

        JideButton homeBtn = new JideButton(ICON_HOME);
        homeBtn.setButtonStyle(JideButton.HYPERLINK_STYLE);
        homeBtn.setToolTipText(stringMgr.getString("navi.button.home.tooltip"));
        homeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!connection.isOnline()) {
                    GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
                    return;
                }
                try {
                    monkeyService.press(connection.getId(), MonkeyConstant.KEYCODE_HOME);
                } catch (MessageException e1) {
                    logger.error("Pressing back button failed:" + e1.getMessage(), e1);
                }
            }
        });
        panel.add(homeBtn);

        box = Box.createHorizontalStrut(gap);
        panel.add(box);

        JideButton wxBtn = new JideButton(ICON_WEIXIN);
        wxBtn.setButtonStyle(JideButton.HYPERLINK_STYLE);
        wxBtn.setToolTipText(stringMgr.getString("navi.button.weixin.tooltip"));
        wxBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startWx();
            }
        });
        panel.add(wxBtn);

        box = Box.createHorizontalStrut(gap);
        panel.add(box);

        JideButton powerBtn = new JideButton(ICON_POWER);
        powerBtn.setButtonStyle(JideButton.HYPERLINK_STYLE);
        powerBtn.setToolTipText(stringMgr.getString("navi.button.power.tooltip"));
        powerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleScreenOnOff();
            }
        });
        panel.add(powerBtn);

        return panel;
    }

    public void showWaitingTip(String text) {
        showWaitingTip(text, WAITING);
    }
    /**
     * Hide screen projection, and show some prompted information
     *
     * @param text the information displayed for user.
     * @param icon the icon object , can be null.
     */
    public void showWaitingTip(String text, Icon icon) {
        tipLabel.setText(text);
        if (tipLabel.getIcon() != icon) {
            tipLabel.setIcon(icon);
        }
        screenComp.setOverlayVisible(true);
        clientScreen.getCanvas().setVisible(false);
    }

    /**
     * Show screen projection, and hide prompted information.
     */
    public void showMonkeyScreen() {
        screenComp.setOverlayVisible(false);
        clientScreen.getCanvas().setVisible(true);
    }

    private void startWx() {
        if (!connection.isOnline()) {
            GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
            return;
        }
        try {
            monkeyService.startActivity(connection.getId(),null, null,null,null,new ArrayList<String>(),new HashMap<String, Object>(),
                    "com.tencent.mm/com.tencent.mm.ui.LauncherUI", 0);
        } catch (MessageException e) {
            logger.error("[" + connection.getId() + "]Starting activity failed:" + e.getMessage(), e);
        }
    }
    private void toggleScreenOnOff() {
        if (!connection.isOnline()) {
            GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
            return;
        }
        try {
            monkeyService.toggleScreenOnOff(connection.getId());
        } catch (MessageException e) {
            GUIUtil.showErrorMessageDialog(e.getMessage());
        }
    }

    private long lastTime;

    private class ShowImageThread extends Thread {
        boolean running;
        public ShowImageThread() {
            super("Show Image");
            setDaemon(true);
            running = true;
        }

        public void stopShow() {
            running = false;
            interrupt();
        }
        @Override
        public void run() {
            while (running) {
                Object obj;
                try {
                    obj = imageQueue.take();
                } catch (InterruptedException e) {
                    logger.warn("The thread of showing image is interrupted.");
                    break;
                }

                Image image;
                if (obj instanceof Image) {
                    image = (Image)obj;
                } else if (obj instanceof byte[]) {
                    image = ImageUtil.readImage((byte[])obj);
                } else {
                    continue;
                }

                clientScreen.showImage(image);
            }
        }
    }

    MonkeyInputListener monkeyInputListener = new MonkeyInputListener() {
        @Override
        public void press(int keyCode) {
            if (!DeviceImageFrame.this.connection.isOnline()) {
                GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
                return;
            }
            try {
                monkeyService.press(DeviceImageFrame.this.connection.getId(), keyCode);
            } catch (MessageException e) {
                logger.error(e.getMessage(), e);
                GUIUtil.showErrorMessageDialog(e.getMessage() , "ERROR");
            }
        }

        @Override
        public void type(String ch) {
            try {
                monkeyService.type(DeviceImageFrame.this.connection.getId(), ch);
            } catch(Exception ex) {
                logger.error(ex.getMessage(), ex);
                GUIUtil.showErrorMessageDialog(ex.getMessage());
            }
        }

        @Override
        public void touchDown(int x, int y) {
            try {
                if (!DeviceImageFrame.this.connection.isOnline()) {
                    GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
                    return;
                }
                lastTime = System.currentTimeMillis();
                monkeyService.touchDown(DeviceImageFrame.this.connection.getId(), x, y);
            } catch (Exception e1) {
                logger.error("[" + DeviceImageFrame.this.connection.getId() + "]Touching down failed:" + e1.getMessage(), e1);
                GUIUtil.showErrorMessageDialog(e1.getMessage());
            }
        }

        @Override
        public void touchUp(int x, int y) {
            if (System.currentTimeMillis() - lastTime > 1000) {
                return;
            }
            try {
                if (!DeviceImageFrame.this.connection.isOnline()) {
                    GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
                    return;
                }
                monkeyService.touchUp(DeviceImageFrame.this.connection.getId(), x, y);
            } catch (MessageException e1) {
                logger.error("[" + DeviceImageFrame.this.connection.getId() + "]Touching up failed:" + e1.getMessage(), e1);
                GUIUtil.showErrorMessageDialog(e1.getMessage());
            }
        }

        @Override
        public void touchMove(int x, int y) {
            if (!DeviceImageFrame.this.connection.isOnline()) {
                GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
                return;
            }
            try {
                monkeyService.touchMove(DeviceImageFrame.this.connection.getId(), x, y);
            } catch (MessageException e1) {
                logger.error("[" + DeviceImageFrame.this.connection.getId() + "]Dragging failed:" + e1.getMessage(), e1);
                GUIUtil.showErrorMessageDialog(e1.getMessage());
            }
        }

        @Override
        public void scroll(int startx, int starty, int endx, int endy, int steps, long ms) {
            if (!DeviceImageFrame.this.connection.isOnline()) {
                GUIUtil.showErrorMessageDialog(stringMgr.getString("status.disconnected"), "ERROR");
                return;
            }
            try {
                if (starty > endy) {
                    monkeyService.scrollUp(DeviceImageFrame.this.connection.getId());
                } else {
                    monkeyService.scrollDown(DeviceImageFrame.this.connection.getId());
                }
            } catch (MessageException e) {
                logger.error("[" + DeviceImageFrame.this.connection.getId() + "]Dragging failed:" + e.getMessage(), e);
                GUIUtil.showErrorMessageDialog(e.getMessage());
            }
        }
    };
}
