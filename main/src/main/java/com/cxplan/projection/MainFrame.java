package com.cxplan.projection;

import com.alee.laf.list.WebListCellRenderer;
import com.cxplan.projection.core.Application;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.model.IDeviceMeta;
import com.cxplan.projection.ui.DeviceImageFrame;
import com.cxplan.projection.ui.component.BaseFrame;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.ui.util.IconUtil;
import com.cxplan.projection.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kenny
 * created on 2018/12/5
 */
public class MainFrame extends BaseFrame {

    private JList list;
    private IApplication application;

    public MainFrame(IApplication application) {
        super("CXProjection");
        this.application = application;

        initView();
        setSize(400, 500);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        loadDevices();
    }

    private void initView() {
        JPanel content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout());

        content.add(new JScrollPane(createDeviceListComponent()), BorderLayout.CENTER);
    }

    private JList createDeviceListComponent() {
        list = new JList<>();
        list.setModel(new DefaultListModel());
        list.setCellRenderer(new DeviceListCellRender(application, true));
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON3) {
//                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
//                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        IDeviceConnection connection = (IDeviceConnection) list.getSelectedValue();
                        showImageFrame(connection);
                    }
                }
            }
        });

        return list;
    }

    private void loadDevices() {

        DefaultListModel model = (DefaultListModel) list.getModel();
        java.util.List<String> deviceIds = application.getDeviceList();
        for (String id : deviceIds) {
            IDeviceConnection connection = application.getDeviceConnection(id);
            model.addElement(connection);
        }
    }

    private void showImageFrame(IDeviceConnection connection) {
        if (!connection.isOnline()) {
            JOptionPane.showMessageDialog(this, "The device is not online");
            return;
        }

        DeviceImageFrame clientFrame = DeviceImageFrame.getInstance(connection.getId());
        if (clientFrame != null) {
            clientFrame.showWindow();
            return;
        }
        if (!DeviceImageFrame.mayBeShow()) {
            GUIUtil.showErrorMessageDialog("超出最大屏幕操作限制");
            return;
        }
        clientFrame = new DeviceImageFrame(connection, application);
        clientFrame.showWindow();

    }

    /**
     * @author KennyLiu
     * @created on 2017/10/27
     */
    private class DeviceListCellRender extends WebListCellRenderer {

        final Icon STATUS_OK = IconUtil.getIcon("/image/device_connected.png");
        final Icon STATUS_NETWORK_ERROR = IconUtil.getIcon("/image/device_network_error.png");
        final Icon STATUS_DISCONNECTED = IconUtil.getIcon(("/image/device_disconnected.png"));

        private IApplication application;
        private boolean showStatusIcon;
        private Set<String> highlightDeviceSet;
        private Color highlightColor;
        private StringBuilder textBuilder;

        public DeviceListCellRender(IApplication application, boolean showStatusIcon) {
            this.application = application;
            this.showStatusIcon = showStatusIcon;
            highlightDeviceSet = new HashSet<>();
            textBuilder = new StringBuilder();

            highlightColor = Color.GREEN.darker();
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            IDeviceMeta deviceMeta = (IDeviceMeta) value;
            String name;
            if (showStatusIcon) {
                Icon icon;
                if (deviceMeta.isOnline()) {
                    if (deviceMeta.isNetworkAvailable()){
                        icon = STATUS_OK;
                    } else {
                        icon = STATUS_NETWORK_ERROR;
                    }
                } else {
                    icon = STATUS_DISCONNECTED;
                }
                setIcon(icon);
            }

            textBuilder.setLength(0);
            textBuilder.append(deviceMeta.getManufacturer()).append("_").
                    append(deviceMeta.getDeviceModel()).
                    append("_").append(deviceMeta.getId());
            setText(textBuilder.toString());

            if (highlightDeviceSet.contains(deviceMeta.getId())) {
                setForeground(highlightColor);
            }

            return this;
        }
    }

}
