package com.cxplan.projection.ui;

import com.alee.extended.window.ComponentMoveAdapter;
import com.alee.global.StyleConstants;
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
import com.cxplan.projection.ui.component.BaseWebFrame;
import com.cxplan.projection.ui.component.IconButton;
import com.cxplan.projection.ui.component.monkey.MonkeyInputListener;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.ui.util.IconUtil;
import com.cxplan.projection.util.ImageUtil;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on 2018/4/7.
 *
 * @author kenny
 */
public class DeviceImageFrame extends BaseWebFrame {
    private static final StringManager stringMgr =
            StringManagerFactory.getStringManager(DeviceImageFrame.class);

    private static final Logger logger = LoggerFactory.getLogger(DeviceImageFrame.class);

    public static final ImageIcon DISCONNECT = IconUtil.getIcon("/image/disconnected.png");
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
    private JPanel toolBarPanel;
    private JLabel tipLabel;

    private IApplication application;
    private IDeviceConnection connection;
    private IDeviceService monkeyService;

    private DeviceConnectionListener deviceConnectionListener;
    private LinkedBlockingQueue<Object> imageQueue = new LinkedBlockingQueue<>(100);
    private ShowImageThread imageThread;

    //Indicate whether there is no frame received since the image channel is connected.
    private boolean isFirstFrame = true;
    private int currentImageWidth = -1;
    private int currentImageHeight = -1;
    //The flag indicates whether current image frame is in projection.
    //When the connection status of image channel is changed, controller will notify device of current status.
    private boolean isInProjection;

    private DeviceImageFrame(IDeviceConnection connection, IApplication application ) {
        super(application.getDeviceName(connection.getId()) + "(" + connection.getId() + ")");
        if (GUIUtil.mainFrame != null) {
            setIconImage(GUIUtil.mainFrame.getIconImage());
        }
        instanceMap.put(connection.getId(), this);
        this.application = application;
        this.connection = connection;
        monkeyService = application.getDeviceService();
        isInProjection = true;

        initView();
        installListener();

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
        calculateFrameSize();
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

    /**
     * Return current status of projection operation.
     * The returned value is always true in projection time including the image channel is interrupted temporarily.
     * the status value is false only when projection frame is disposed.
     *
     * When the connection status of image channel is changed, controller will notify device of current status.
     *
     * @return true: in projection status.
     *         false: The projection operation is terminated.
     */
    public boolean isInProjection() {
        return isInProjection;
    }

    @Override
    public void dispose() {
        isInProjection = false;
        connection.closeImageChannel();
        application.removeDeviceConnectionListener(deviceConnectionListener);
        super.dispose();
        if (imageThread != null) {
            imageThread.stopShow();
        }
        instanceMap.remove(connection.getId());
    }

    public void setToolBarVisible(boolean visible) {
        if (visible == toolBarPanel.isVisible()) {
            return;
        }

        toolBarPanel.setVisible(visible);
        adaptFrameSize();
    }

    public void setNavigationBarVisible(boolean visible) {
        if (visible) {
            clientScreen.showExtComponent();
            adaptFrameSize();
        } else {
            clientScreen.hideExtComponent();
            adaptFrameSize();
        }
    }

    private void openScreenProjection() {
        if (connection.isConnected()) {
            openImageChannel();
        } else {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    String path = application.getInfrastructureService().getMainPackageInstallPath(connection.getId());
                    if (path == null) {
                        String installTip = stringMgr.getString("status.main_process.install");
                        showWaitingTip(installTip);
                        try {
                            application.getInfrastructureService().installMainProcess(connection.getDevice());
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                            String installFailText = stringMgr.getString("status.install.fail");
                            GUIUtil.showErrorMessageDialog(installFailText + ": " + ex.getMessage());
                            return;
                        }
                    }
                    showWaitingTip(stringMgr.getString("status.connecting"));
                    connection.connect();
                }
            };
            application.getExecutors().submit(task);
        }
    }

    /**
     * Open image channel when connection is connected.
     * This method will create a connection with image service run in device,
     * and then wait util a image data is coming. If the image channel is on connected status, do nothing.
     */
    public void openImageChannel() {
        if (!connection.isImageChannelAvailable()) {
            showWaitingTip(stringMgr.getString("status.connecting"));
            boolean ret = connection.openImageChannel(new ConnectStatusListener() {
                @Override
                public void OnSuccess(IDeviceConnection connection) {
                    try {
                        application.getDeviceService().wake(connection.getId());
                    } catch (MessageException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                @Override
                public void onFailed(IDeviceConnection connection, String error) {
                    GUIUtil.showErrorMessageDialog(error, stringMgr.getString("connect.fail"));
                }
            });
            if (ret) {
                showWaitingTip(stringMgr.getString("status.waitimage"));
                isFirstFrame = true;
            }
        } else {
            showWaitingTip(stringMgr.getString("status.waitimage"));
            isFirstFrame = true;
        }
    }

    public void waitImageChannelChanged() {
        waitImageChannelChanged(null);
    }
    /**
     * When some image parameters are changed, the image service may make some modification.
     * Client should wait a moment util modification is completed.
     */
    public void waitImageChannelChanged(String msg) {
        String promptText = msg;
        if (promptText == null) {
            promptText = stringMgr.getString("status.wait.config.changed");
        }
        showWaitingTip(promptText);
        connection.closeImageChannel();
    }

    private void initView() {
        ComponentMoveAdapter.install ( getRootPane (), DeviceImageFrame.this );
        setShowResizeCorner(true);

        JPanel mainContent = (JPanel)getContentPane();
        mainContent.setLayout(new BorderLayout());

        //toolbar
        toolBarPanel = createToolBar();
        mainContent.add(toolBarPanel, BorderLayout.NORTH);

        clientScreen = new DeviceDisplayPanel(getGraphicsConfiguration(), monkeyInputListener);
        clientScreen.setBorder(BorderFactory.createEmptyBorder());
        clientScreen.setBackground(Color.gray);
        clientScreen.setDeviceZoomRate(connection.getZoomRate());
        clientScreen.setExtComponent(createDeviceButtonPanel());
        screenComp = new DefaultOverlayable(clientScreen);
        screenComp.setBorder(BorderFactory.createEmptyBorder());
        tipLabel = new JLabel();
        screenComp.addOverlayComponent(tipLabel,
                SwingConstants.CENTER);
        screenComp.setOverlayVisible(true);
        mainContent.add(screenComp, BorderLayout.CENTER);

        boolean showNaviBar = Setting.getInstance().getBooleanProperty(connection.getId(),
                SettingConstant.KEY_DEVICE_NAVI_VISIBLE, true);
        if (showNaviBar) {
            clientScreen.showExtComponent();
        } else {
            clientScreen.hideExtComponent();
        }
    }

    /**
     * This method is invoked when initialize frame size, Calculate a suitable size
     * matched with displayed image for frame. The size result should refer to the rotation of current screen.
     */
    private void calculateFrameSize() {
        int prefWidth = (int) (connection.getScreenWidth() * connection.getZoomRate());
        int prefHeight = (int) (connection.getScreenHeight() * connection.getZoomRate());

        if (connection.getRotation() % 2 == 0) {
            checkImageSizeChanged(prefWidth, prefHeight);
        } else {
            checkImageSizeChanged(prefHeight, prefWidth);
        }
        clientScreen.getCanvas().setVisible(false);
    }

    private JPanel createToolBar() {
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createLineBorder(StyleConstants.borderColor, 1, true));
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.X_AXIS));

        //setting button
        IconButton settingBtn = new IconButton(IconUtil.getIcon("/image/device/setting.png"));
        String tip = stringMgr.getString("toolbar.setting.tip");
        settingBtn.setToolTipText(tip);
        settingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DeviceSettingDialog settingDialog = new DeviceSettingDialog(DeviceImageFrame.this, connection);
                GUIUtil.centerToOwnerWindow(settingDialog);
                settingDialog.setVisible(true);
            }
        });
        pane.add(settingBtn, JideBoxLayout.FIX);

        //screenshot
        IconButton screenshotBtn = new IconButton(IconUtil.getIcon("/image/device/screenshot.png"));
        tip = stringMgr.getString("toolbar.screenshot.tip");
        screenshotBtn.setToolTipText(tip);
        screenshotBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Image image;
                try {
                    image = monkeyService.takeScreenshot(connection.getId(), 1.0f, 80);
                } catch (MessageException ex) {
                    logger.error(ex.getMessage(), ex);
                    GUIUtil.showErrorMessageDialog(ex.getMessage());
                    return;
                }

                File file = GUIUtil.saveFile(DeviceImageFrame.this, System.currentTimeMillis() + ".jpg");
                if (file == null) {
                    return;
                }

                try {
                    ImageUtil.image2File(image, "JPG", file);
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                    GUIUtil.showErrorMessageDialog(ex.getMessage());
                    return;
                }
            }
        });
        pane.add(screenshotBtn, JideBoxLayout.FIX);

        //volume up
        IconButton volumeUpBtn = new IconButton(IconUtil.getIcon("/image/device/volume_up.png"));
        tip = stringMgr.getString("toolbar.volumeup.tip");
        volumeUpBtn.setToolTipText(tip);
        volumeUpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    monkeyService.press(connection.getId(), MonkeyConstant.KEYCODE_VOLUME_UP);
                } catch (MessageException e1) {
                    logger.error(e1.getMessage(), e1);
                    GUIUtil.showErrorMessageDialog(e1.getMessage());
                }
            }
        });
        pane.add(volumeUpBtn, JideBoxLayout.FIX);
        //volume down
        IconButton volumeDownBtn = new IconButton(IconUtil.getIcon("/image/device/volume_down.png"));
        tip = stringMgr.getString("toolbar.volumedown.tip");
        volumeDownBtn.setToolTipText(tip);
        volumeDownBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    monkeyService.press(connection.getId(), MonkeyConstant.KEYCODE_VOLUME_DOWN);
                } catch (MessageException ex) {
                    logger.error(ex.getMessage(), ex);
                    GUIUtil.showErrorMessageDialog(ex.getMessage());
                }
            }
        });
        pane.add(volumeDownBtn, JideBoxLayout.FIX);

        return pane;
    }

    private void installListener() {
        deviceConnectionListener = new DeviceConnectionListener() {
            @Override
            public boolean frameReady(DeviceConnectionEvent event) {
                if(!checkConnection(event)) {
                    return false;
                }

                if (isFirstFrame) {
                    imageQueue.clear();
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
                showWaitingTip("", DISCONNECT);
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
                    isFirstFrame = true;
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
                showWaitingTip(stringMgr.getString("status.connecting"));
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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
//                clientScreen.refreshImage();
            }
        });

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

    private JPanel createDeviceButtonPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.LINE_AXIS));

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
        panel.add(backBtn, JideBoxLayout.FLEXIBLE);

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
        panel.add(homeBtn, JideBoxLayout.FLEXIBLE);

        JideButton wxBtn = new JideButton(ICON_WEIXIN);
        wxBtn.setButtonStyle(JideButton.HYPERLINK_STYLE);
        wxBtn.setToolTipText(stringMgr.getString("navi.button.weixin.tooltip"));
        wxBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startWx();
            }
        });
        panel.add(wxBtn, JideBoxLayout.FLEXIBLE);

        JideButton powerBtn = new JideButton(ICON_POWER);
        powerBtn.setButtonStyle(JideButton.HYPERLINK_STYLE);
        powerBtn.setToolTipText(stringMgr.getString("navi.button.power.tooltip"));
        powerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleScreenOnOff();
            }
        });
        panel.add(powerBtn, JideBoxLayout.FLEXIBLE);

        return panel;
    }

    public void showWaitingTip(String text) {
        showWaitingTip(text, WAITING);
    }
    /**
     * Hide screen projection, and show a prompted information.
     * When projection is interrupted temporarily, this method should be invoked,
     * it can take a good experience to user.
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
                checkImageSizeChanged(image.getWidth(null), image.getHeight(null));
            }
        }
    }

    private void checkImageSizeChanged(int newWidth, int newHeight) {
        if (currentImageWidth != newWidth || currentImageHeight != newHeight) {
            currentImageHeight = newHeight;
            currentImageWidth = newWidth;

            //update zoom rate of canvas.
            //Warning: this zoom rate is different from the rate of image.
            double newCanvasZoomRate;
            if ((connection.getRotation() % 2) == 0) {
                newCanvasZoomRate = (double) currentImageWidth / connection.getScreenWidth();
            } else {
                newCanvasZoomRate = (double) currentImageWidth / connection.getScreenHeight();
            }
            clientScreen.setDeviceZoomRate(newCanvasZoomRate);
            adaptFrameSize();
        }

    }
    private void adaptFrameSize() {
        int maxHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.8);
        int optimalWidth = currentImageWidth;
        int optimalHeight = currentImageHeight;
        if (optimalHeight > maxHeight) {
            optimalWidth = (int)((double)maxHeight * optimalWidth / optimalHeight);
            optimalHeight = maxHeight;
        }

        clientScreen.setCanvasSize(optimalWidth, optimalHeight);

        pack();
        logger.info("Frame size is changed:[{}x{}]", getWidth(), getHeight());
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
                monkeyService.touchDown(DeviceImageFrame.this.connection.getId(), x, y);
            } catch (Exception e1) {
                logger.error("[" + DeviceImageFrame.this.connection.getId() + "]Touching down failed:" + e1.getMessage(), e1);
                GUIUtil.showErrorMessageDialog(e1.getMessage());
            }
        }

        @Override
        public void touchUp(int x, int y) {
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
        public void scroll(int startx, int starty, int endx, int endy) {
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
