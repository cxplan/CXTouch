package com.cxplan.projection;

import com.alee.global.StyleConstants;
import com.alee.utils.WebUtils;
import com.cxplan.projection.core.ServiceFactory;
import com.cxplan.projection.core.connection.DeviceConnectionAdapter;
import com.cxplan.projection.core.connection.DeviceConnectionEvent;
import com.cxplan.projection.core.connection.DeviceConnectionListener;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.core.setting.ConfigChangedListener;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.core.setting.SettingConstant;
import com.cxplan.projection.core.setting.SettingEvent;
import com.cxplan.projection.i18n.StringManager;
import com.cxplan.projection.i18n.StringManagerFactory;
import com.cxplan.projection.net.message.MessageException;
import com.cxplan.projection.service.IInfrastructureService;
import com.cxplan.projection.ui.DeviceImageFrame;
import com.cxplan.projection.ui.DeviceSettingDialog;
import com.cxplan.projection.ui.component.BaseFrame;
import com.cxplan.projection.ui.component.IconButton;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.ui.util.IconUtil;
import com.cxplan.projection.util.SystemUtil;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.JideBoxLayout;
import info.clearthought.layout.TableLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kenny
 * created on 2018/12/5
 */
public class MainFrame extends BaseFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private static final StringManager stringMgr =
            StringManagerFactory.getStringManager(MainFrame.class);

    private JPanel deviceListPane;//The panel where all device are placed.
    private DefaultOverlayable deviceOverlayable;
    private IApplication application;
    private Map<String , DeviceComponent> deviceMap;
    private DeviceConnectionListener connectionListener;

    public MainFrame(IApplication application) {
        super("CXTouch");
        this.application = application;

        JPanel c = (JPanel) getContentPane();
        c.setLayout(new BorderLayout());
        initView();
        setSize(500, 600);

        connectionListener = new DeviceConnectionChangedListener();
        application.addDeviceConnectionListener(connectionListener);
        Setting.getInstance().addPropertyChangeListener(new DeviceSettingListener());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        deviceMap = new HashMap<>();
        loadDevices();
    }

    private void initView() {
        JPanel content = new JPanel();
        setContentPane(content);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15,10,15));
        content.setBackground(Color.WHITE);
        content.setLayout(new JideBoxLayout(content, JideBoxLayout.PAGE_AXIS, 6));

        //device list panel
        JLabel label = new JLabel(stringMgr.getString("panel.title.devicelist"));
        content.add(label, JideBoxLayout.FIX);
        Border border = BorderFactory.createLineBorder(StyleConstants.borderColor, 1, true);
        deviceListPane = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                if (dim.height < 50) {
                    dim.height = 50;
                }

                return dim;
            }
        };
        deviceListPane.setBorder(border);
        deviceListPane.setLayout(new JideBoxLayout(deviceListPane, JideBoxLayout.PAGE_AXIS));
        deviceOverlayable = new DefaultOverlayable(deviceListPane);
        deviceOverlayable.addOverlayComponent(createNoDevicePane());
        deviceOverlayable.setOverlayVisible(false);
        content.add(deviceOverlayable, JideBoxLayout.FIX);

        //separator
        content.add(Box.createVerticalStrut(30), JideBoxLayout.FIX);

        //information
        String infoLabel = stringMgr.getString("panel.title.info");
        content.add(new JLabel(infoLabel), JideBoxLayout.FIX);
        content.add(createInfoPane(), JideBoxLayout.FIX);
    }

    private JPanel createNoDevicePane() {
        JPanel pane = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                if (dim.height < 50) {
                    dim.height = 50;
                }
                dim.width = deviceListPane.getWidth();

                return dim;
            }
        };
        Color color = new Color(255, 223, 178);
        Border border = BorderFactory.createLineBorder(color.darker(), 1, true);
        pane.setBorder(border);
        pane.setBackground(color);

        String noDeviceText = stringMgr.getString("pane.devicelist.nodevice");
        pane.add(new JLabel(noDeviceText));

        return pane;
    }

    private JPanel createInfoPane() {
        final JPanel infoPane = new JPanel();
        Border outBorder= BorderFactory.createLineBorder(StyleConstants.borderColor, 1, true);
        Border inBorder = BorderFactory.createEmptyBorder(4, 4,4,4);
        infoPane.setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));
        infoPane.setLayout(new BorderLayout());

        //statement: this is a open source software which control and manage android device from PC client.
        String text = stringMgr.getString("info.statement");
        JTextArea textArea = new JTextArea(text);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        infoPane.add(textArea, BorderLayout.NORTH);

        //ADB driver download
        if (SystemUtil.isWindow()) {
            String adbDriverPrompt = stringMgr.getString("panel.adb.driver.prompt");
            String driverTitle = stringMgr.getString("panel.adb.driver.title");
            final String driverUrl = stringMgr.getString("panel.adb.driver.url");
            //prompt label
            JLabel promptLabel = new JLabel(adbDriverPrompt);
            //adb driver url label
            JLabel adbDriverLabel = new JLabel(driverTitle);
            adbDriverLabel.setForeground(StyleConstants.topFocusedBgColor);
            adbDriverLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            adbDriverLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            WebUtils.browseSiteSafely ( driverUrl );
                        }
                    };

                    application.getExecutors().submit(task);
                }
            });

            JPanel adbDriverPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
            adbDriverPane.add(promptLabel);
            adbDriverPane.add(adbDriverLabel);

            infoPane.add(adbDriverPane, BorderLayout.CENTER);
        }

        //contact: author ,mail and weixin
        JPanel contactPane = new JPanel();
        double p = TableLayout.PREFERRED;
        double gap = 4;
        double[][] size = new double[][]{{gap, p, 10, TableLayout.FILL},
                {
                    20, p,
                    gap, p,
                    gap, p,
                    gap, p
                }};
        contactPane.setLayout(new TableLayout(size));
        //project site
        String projectSiteLabel = stringMgr.getString("info.project.site.label");
        final String projectSiteUrl = stringMgr.getString("info.project.site.url");
        final JLabel pslLabel = new JLabel(projectSiteUrl);
        pslLabel.setForeground(StyleConstants.topFocusedBgColor);
        pslLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pslLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        WebUtils.browseSiteSafely ( projectSiteUrl );
                    }
                };

                application.getExecutors().submit(task);
            }
        });
        contactPane.add(new JLabel(projectSiteLabel), "1,1");
        contactPane.add(pslLabel, "3,1");
        //author
        String author = stringMgr.getString("info.author");
        contactPane.add(new JLabel(author), "1,3");
        contactPane.add(new JLabel("Kenny Liu"), "3,3");
        //mail
        String mailLabel = stringMgr.getString("info.mail.label");
        final String mail = stringMgr.getString("info.mail.address");
        JLabel mailLinkLabel = new JLabel(mail);
        mailLinkLabel.setForeground(StyleConstants.topFocusedBgColor);
        mailLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mailLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        WebUtils.writeEmailSafely ( mail );
                    }
                };

                application.getExecutors().submit(task);
            }
        });
        contactPane.add(new JLabel(mailLabel), "1,5");
        contactPane.add(mailLinkLabel, "3,5");

        //version
        String versionLabel = stringMgr.getString("info.version.label");
        contactPane.add(new JLabel(versionLabel), "1,7");
        contactPane.add(new JLabel("1.0"), "3,7");

        infoPane.add(contactPane, BorderLayout.SOUTH);
        return infoPane;
    }

    private void loadDevices() {

        java.util.List<String> deviceIds = application.getDeviceList();
        for (String id : deviceIds) {
            final IDeviceConnection connection = application.getDeviceConnection(id);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addDevice(connection);
                }
            });
        }

        if (deviceListPane.getComponentCount() == 0) {
            deviceOverlayable.setOverlayVisible(true);
        }
    }

    /**
     * Open device projection window. If the projection window exists already,
     * return it directly.
     *
     * @param connection device connection which will be viewed.
     */
    private void showImageFrame(IDeviceConnection connection) {

        if (!connection.isConnected()) {
            String notPreparedText = stringMgr.getString("mainframe.device.not.prepared");
            GUIUtil.showErrorMessageDialog(notPreparedText);
            return;
        }

        DeviceImageFrame clientFrame;
        try {
            clientFrame = DeviceImageFrame.getInstance(connection.getId(), application, true);
        } catch (Exception ex) {
            GUIUtil.showErrorMessageDialog(ex.getMessage(), "ERROR");
            return;
        }
        if (clientFrame != null) {
            clientFrame.showWindow();
            return;
        }

    }

    public void addDevice(IDeviceConnection connection) {
        if (deviceMap.containsKey(connection.getId())) {
            return;
        }
        DeviceComponent dc = new DeviceComponent(connection);
        deviceMap.put(connection.getId(), dc);

        boolean first = deviceListPane.getComponentCount() == 0;

        deviceListPane.add(dc, deviceListPane.getComponentCount());
        if (first) {
            deviceOverlayable.setOverlayVisible(false);
        } else {
            ((DeviceComponent)deviceListPane.getComponent(deviceListPane.getComponentCount() - 2)).setSeparatorBorder(true);
        }
        deviceListPane.invalidate();
        deviceListPane.validate();
        deviceListPane.repaint();
    }

    public void removeDevice(IDeviceConnection connection) {
        DeviceComponent dc = deviceMap.remove(connection.getId());
        if (dc == null) {
            return;
        }

        int count = deviceListPane.getComponentCount();
        boolean shouldUpdateBorder = false;
        for (int i = 0; i < count; i++) {
            Component comp = deviceListPane.getComponent(i);
            if (comp == dc) {
                if (i == (count - 1) && count > 1) {
                    shouldUpdateBorder = true;
                }
                deviceListPane.remove(i);
            }
        }

        if (shouldUpdateBorder) {
            ((DeviceComponent)deviceListPane.getComponent(count - 2)).setSeparatorBorder(false);
        }
        deviceListPane.invalidate();
        deviceListPane.validate();
        deviceListPane.repaint();

        if (deviceListPane.getComponentCount() == 0) {
            deviceOverlayable.setOverlayVisible(true);
        }
    }

    private void changeDeviceName(String deviceId, String name) {
        int count = deviceListPane.getComponentCount();
        for (int i = 0; i < count; i++) {
            DeviceComponent dc = (DeviceComponent)deviceListPane.getComponent(i);
            if (dc.connection.getId().equals(deviceId)) {
                dc.connection.setDeviceName(name);
                dc.updateName();
            }
        }
    }

    private static final Border DEVICE_BORDER_NONE = BorderFactory.createEmptyBorder(2, 5, 2, 2);
    private static final Border DEVICE_BORDER_SEPARATOR = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, StyleConstants.borderColor),
            DEVICE_BORDER_NONE);
    /**
     * The device item component comprised of device name, serial number,
     * and some operation buttons.
     *
     */
    private class DeviceComponent extends JPanel {
        private IDeviceConnection connection;
        private JLabel deviceNameLabel;
        private JLabel serialLabel;

        public DeviceComponent(final IDeviceConnection connection) {
            this.connection = connection;
            setBorder(DEVICE_BORDER_NONE);
            Color background = MainFrame.this.getContentPane().getBackground();
            setBackground(background);
            setLayout(new JideBoxLayout(this, JideBoxLayout.LINE_AXIS));

            JPanel labelPane = new JPanel();
            labelPane.setBackground(background);
            labelPane.setLayout(new JideBoxLayout(labelPane, JideBoxLayout.Y_AXIS));
            deviceNameLabel = new JLabel(application.getDeviceName(connection.getId()));
            labelPane.add(deviceNameLabel, JideBoxLayout.FIX);
            serialLabel = new JLabel("Serial: " + connection.getId());
            serialLabel.setForeground(StyleConstants.infoTextColor);
            labelPane.add(serialLabel, JideBoxLayout.FIX);

            add(labelPane, JideBoxLayout.VARY);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(background);
            //view button
            IconButton viewBtn = new IconButton(IconUtil.getIcon("/image/view.png"));
            String viewBtnTooltip = stringMgr.getString("view.button.tooltip");
            viewBtn.setToolTipText(viewBtnTooltip);
            viewBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showImageFrame(connection);
                }
            });
            buttonPanel.add(viewBtn);
            //setting button.
            IconButton settingBtn = new IconButton(IconUtil.getIcon("/image/setting.png"));
            String settingBtnTooltip = stringMgr.getString("setting.button.tooltip");
            settingBtn.setToolTipText(settingBtnTooltip);
            settingBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DeviceSettingDialog settingDialog = new DeviceSettingDialog(MainFrame.this, connection);
                    GUIUtil.centerToOwnerWindow(settingDialog);
                    settingDialog.setVisible(true);
                }
            });
            buttonPanel.add(settingBtn);

            add(buttonPanel, JideBoxLayout.FIX);

            Font font = deviceNameLabel.getFont();
            font = new Font(font.getName(), font.getStyle(), 13);
            deviceNameLabel.setFont(font);
            serialLabel.setFont(font);
        }

        public void updateName() {
            deviceNameLabel.setText(application.getDeviceName(connection.getId()));
        }

        public void setSeparatorBorder(boolean value) {
            if (value) {
                setBorder(DEVICE_BORDER_SEPARATOR);
            } else {
                setBorder(DEVICE_BORDER_NONE);
            }
        }
    }


    private class DeviceConnectionChangedListener extends DeviceConnectionAdapter {
        @Override
        public void created(final DeviceConnectionEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addDevice(event.getSource());
                }
            });
        }
        @Override
        public void removed(final DeviceConnectionEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    IDeviceConnection removedObj = event.getSource();
                    removeDevice(removedObj);
                }
            });

        }

        @Override
        public boolean frameReady(DeviceConnectionEvent event) {
            return false;
        }

        @Override
        public void connected(DeviceConnectionEvent event) {
            //send image projection event
            if (event.getType() == DeviceConnectionEvent.ConnectionType.IMAGE) {
                IInfrastructureService infrastructureService = application.getInfrastructureService();
                infrastructureService.notifyProjectionFlag(event.getSource().getId(), true);
            }
        }

        @Override
        public void connectionClosed(DeviceConnectionEvent event) {
            //send image projection event
            if (event.getType() == DeviceConnectionEvent.ConnectionType.IMAGE) {
                IInfrastructureService infrastructureService = application.getInfrastructureService();
                infrastructureService.notifyProjectionFlag(event.getSource().getId(), false);
            }
        }
    }

    private class DeviceSettingListener implements ConfigChangedListener {

        @Override
        public void changed(SettingEvent event) {
            if (event.isSystemSetting()) {
                return;
            }

            if (event.getPropertyName().equals(SettingConstant.KEY_DEVICE_NAME)) {
                changeDeviceName(event.getSource(), (String) event.getNewValue());
            } else if (event.getPropertyName().equals(SettingConstant.KEY_DEVICE_ALWAYS_TOP)) {
                DeviceImageFrame clientFrame = DeviceImageFrame.getInstance(event.getSource(), application, false);
                if (clientFrame == null) {
                    return;
                }
                clientFrame.setAlwaysOnTop((Boolean)event.getNewValue());
            } else if (event.getPropertyName().equals(SettingConstant.KEY_DEVICE_NAVI_VISIBLE)) {
                DeviceImageFrame clientFrame = DeviceImageFrame.getInstance(event.getSource(), application, false);
                if (clientFrame == null) {
                    return;
                }
                clientFrame.setNavigationBarVisible((Boolean)event.getNewValue());
            } else if (event.isResult()){
                String[] keys = event.getChangedKeys();
                boolean isHit = false;
                for (String key : keys) {
                    if (key.equals(SettingConstant.KEY_DEVICE_IMAGE_QUALITY)
                            || key.equals(SettingConstant.KEY_DEVICE_IMAGE_ZOOM_RATE)) {
                        isHit = true;
                        break;
                    }
                }

                if (isHit) {
                    processImageConfigChanged(event.getSource());
                }
            }
        }
    }

    private void processImageConfigChanged(final String deviceId) {
        final DeviceImageFrame instance = DeviceImageFrame.getInstance(deviceId, application, false);
        if (instance != null) {
            instance.waitImageChannelChanged();
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                IInfrastructureService infrastructureService = application.getInfrastructureService();
                try {
                    infrastructureService.startMinicapService(deviceId);
                } catch (MessageException e) {
                    logger.error(e.getMessage(), e);
                    GUIUtil.showErrorMessageDialog(e.getMessage());
                }

                instance.openImageChannel();
            }
        };
        application.getExecutors().submit(task);

    }
}
