package com.cxplan.projection.ui;

import com.alee.global.StyleConstants;
import com.cxplan.projection.core.connection.IDeviceConnection;
import com.cxplan.projection.core.setting.Setting;
import com.cxplan.projection.core.setting.SettingConstant;
import com.cxplan.projection.i18n.StringManager;
import com.cxplan.projection.i18n.StringManagerFactory;
import com.cxplan.projection.ui.component.BaseDialog;
import com.cxplan.projection.ui.component.ItemMeta;
import com.cxplan.projection.util.StringUtil;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * @author Kenny
 * created on 2018/12/13
 */
public class DeviceSettingDialog extends BaseDialog {
    private static final StringManager stringMgr =
            StringManagerFactory.getStringManager(DeviceSettingDialog.class);

    private JTextField nameField;
    private JCheckBox toolBarDisplayField;
    private JCheckBox naviBarDisplayField;
    private JCheckBox alwaysTopField;

    private JComboBox<ItemMeta> qualityField;
    private JComboBox<ItemMeta> zoomRateField;

    private IDeviceConnection connection;

    public DeviceSettingDialog(Window parent, IDeviceConnection connection) {
        super(parent, connection.getDeviceModel() + stringMgr.getString("device.setting.dialog.title"), true);
        this.connection = connection;

        initView();
        initData();
        setSize(500, 550);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void initView() {
        JPanel contentPane = new JPanel();
        setContentPane(new JScrollPane(contentPane));
        contentPane.setLayout(new JideBoxLayout(contentPane, JideBoxLayout.PAGE_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 14,10,14));
        contentPane.setBackground(Color.WHITE);
        //name panel
        JPanel namePane = new JPanel();
        Border outBorder= BorderFactory.createLineBorder(StyleConstants.borderColor, 1, true);
        Border inBorder = BorderFactory.createEmptyBorder(5, 10,5,10);
        namePane.setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));
        namePane.setLayout(new JideBoxLayout(namePane, JideBoxLayout.LINE_AXIS));
        String nameLabel = stringMgr.getString("device.setting.name.label");
        namePane.add(new JLabel(nameLabel), JideBoxLayout.FIX);
        nameField = new JTextField();
        namePane.add(nameField, JideBoxLayout.VARY);
        contentPane.add(namePane, JideBoxLayout.FIX);

        contentPane.add(Box.createVerticalStrut(30), JideBoxLayout.FIX);

        //window panel
        String windowPaneLabel = stringMgr.getString("device.setting.window.label");
        contentPane.add(new JLabel(windowPaneLabel), JideBoxLayout.FIX);
        contentPane.add(createWindowPanel(), JideBoxLayout.FIX);

        contentPane.add(Box.createVerticalStrut(30), JideBoxLayout.FIX);

        //Image quality
        String qualityLabel = stringMgr.getString("device.setting.window.quality.label");
        contentPane.add(new JLabel(qualityLabel), JideBoxLayout.FIX);
        contentPane.add(createImageQualityPanel(), JideBoxLayout.FIX);

        contentPane.add(Box.createVerticalStrut(40), JideBoxLayout.FIX);

        //button panel
        contentPane.add(createButtonPane(), JideBoxLayout.VARY);
    }

    private JPanel createWindowPanel() {
        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.Y_AXIS, 10));
        Border outBorder= BorderFactory.createLineBorder(StyleConstants.borderColor, 1, true);
        Border inBorder = BorderFactory.createEmptyBorder(10, 10,10,10);
        pane.setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));

        //toolbar
        String toolbarLabel = stringMgr.getString("device.setting.window.toolbar.label");
        toolBarDisplayField = new JCheckBox(toolbarLabel);
        pane.add(toolBarDisplayField, JideBoxLayout.FIX);
        //navi bar
        String navibarLabel = stringMgr.getString("device.setting.window.navibar.label");
        naviBarDisplayField = new JCheckBox(navibarLabel);
        pane.add(naviBarDisplayField, JideBoxLayout.FIX);
        //always top
        String alwaysTopLabel = stringMgr.getString("device.setting.window.alwaystop.label");
        alwaysTopField = new JCheckBox(alwaysTopLabel);
        pane.add(alwaysTopField, JideBoxLayout.FIX);

        return pane;
    }

    private JPanel createImageQualityPanel() {
        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.Y_AXIS, 5));
        Border outBorder= BorderFactory.createLineBorder(StyleConstants.borderColor, 1, true);
        Border inBorder = BorderFactory.createEmptyBorder(10, 10,10,10);
        pane.setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));

        //quality
        String qualityLabel = stringMgr.getString("device.setting.image.quality.label");
        pane.add(new JLabel(qualityLabel), JideBoxLayout.FIX);
        qualityField = new JComboBox<>();
        qualityField.addItem(new ItemMeta<>("20", 20));
        qualityField.addItem(new ItemMeta<>("40", 40));
        qualityField.addItem(new ItemMeta<>("60", 60));
        qualityField.addItem(new ItemMeta<>("80", 80));
        qualityField.addItem(new ItemMeta<>("100", 100));
        qualityField.setSelectedIndex(3);
        pane.add(qualityField, JideBoxLayout.FIX);

        pane.add(Box.createVerticalStrut(20), JideBoxLayout.FIX);

        //Zoom rate
        final String zoomRateLabel = stringMgr.getString("device.setting.image.zoomrate.label");
        final JLabel displaySizeLabel = new JLabel(zoomRateLabel);
        pane.add(displaySizeLabel, JideBoxLayout.FIX);
        zoomRateField = new JComboBox<>();
        zoomRateField.addItem(new ItemMeta<>("20%", 0.2f));
        zoomRateField.addItem(new ItemMeta<>("40%", 0.4f));
        zoomRateField.addItem(new ItemMeta<>("50%", 0.5f));
        zoomRateField.addItem(new ItemMeta<>("60%", 0.6f));
        zoomRateField.addItem(new ItemMeta<>("80%", 0.8f));
        zoomRateField.addItem(new ItemMeta<>("100%", 1.0f));
        zoomRateField.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    float zoomRate = (Float) ((ItemMeta)zoomRateField.getSelectedItem()).getValue();
                    int width = (int) (connection.getScreenHeight() * zoomRate);
                    int height = (int) (connection.getScreenWidth() * zoomRate);
                    String text = zoomRateLabel + "(" + width + "x" + height + ")";
                    displaySizeLabel.setText(text);
                }
            }
        });
        pane.add(zoomRateField, JideBoxLayout.FIX);

        return pane;
    }

    private JPanel createButtonPane() {
        JPanel pane = new JPanel();
        pane.setBackground(Color.WHITE);
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));

        String saveBtnLabel = stringMgr.getString("device.setting.button.save.label");
        JButton saveButton = new JButton(saveBtnLabel);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        pane.add(saveButton);

        String cancelBtnLabel = stringMgr.getString("device.setting.button.cancel.label");
        JButton cancelBtn = new JButton(cancelBtnLabel);
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pane.add(cancelBtn);

        return pane;
    }

    private void initData() {
        String deviceId = connection.getId();
        Setting setting = Setting.getInstance();

        nameField.setText(setting.getProperty(deviceId, SettingConstant.KEY_DEVICE_NAME, ""));

        //toolbar
        toolBarDisplayField.setSelected(setting.getBooleanProperty(deviceId, SettingConstant.KEY_DEVICE_TOOLBAR_VISIBLE, true));
        //navigation bar
        naviBarDisplayField.setSelected(setting.getBooleanProperty(deviceId, SettingConstant.KEY_DEVICE_NAVI_VISIBLE, true));
        //always top
        alwaysTopField.setSelected(setting.getBooleanProperty(deviceId, SettingConstant.KEY_DEVICE_ALWAYS_TOP, false));

        //quality
        int quality = setting.getIntProperty(deviceId, SettingConstant.KEY_DEVICE_IMAGE_QUALITY, 80);
        int count = qualityField.getItemCount();
        for (int i = 0; i < count; i++) {
            if ((int)qualityField.getItemAt(i).getValue() == quality) {
                qualityField.setSelectedIndex(i);
            }
        }
        //zoom rate
        float zoomRate = setting.getFloatProperty(deviceId, SettingConstant.KEY_DEVICE_IMAGE_ZOOM_RATE, 0.5f);
        count = zoomRateField.getItemCount();
        for (int i = 0; i < count; i++) {
            if ((float)zoomRateField.getItemAt(i).getValue() == zoomRate) {
                zoomRateField.setSelectedIndex(i);
            }
        }
    }

    private void save() {
        String deviceId = connection.getId();
        Setting setting = Setting.getInstance();

        java.util.List<String> changedKeyList = new ArrayList<>(6);
        boolean changed = false;

        String name = nameField.getText().trim();
        if (StringUtil.isNotEmpty(name)) {
            changed = setting.putProperty(deviceId, SettingConstant.KEY_DEVICE_NAME, name);
        } else {
            changed = setting.putProperty(deviceId, SettingConstant.KEY_DEVICE_NAME, null);
        }
        if (changed) {
            changedKeyList.add(SettingConstant.KEY_DEVICE_NAME);
        }

        //toolbar
        changed = setting.putBooleanProperty(deviceId, SettingConstant.KEY_DEVICE_TOOLBAR_VISIBLE, toolBarDisplayField.isSelected());
        if (changed) {
            changedKeyList.add(SettingConstant.KEY_DEVICE_TOOLBAR_VISIBLE);
        }
        //navigation bar
        changed = setting.putBooleanProperty(deviceId, SettingConstant.KEY_DEVICE_NAVI_VISIBLE, naviBarDisplayField.isSelected());
        if (changed) {
            changedKeyList.add(SettingConstant.KEY_DEVICE_NAVI_VISIBLE);
        }
        //always top
        changed = setting.putBooleanProperty(deviceId, SettingConstant.KEY_DEVICE_ALWAYS_TOP, alwaysTopField.isSelected());
        if (changed) {
            changedKeyList.add(SettingConstant.KEY_DEVICE_ALWAYS_TOP);
        }

        //quality
        ItemMeta<Integer> qualityItem = (ItemMeta<Integer>) qualityField.getSelectedItem();
        changed = setting.putIntProperty(deviceId, SettingConstant.KEY_DEVICE_IMAGE_QUALITY, qualityItem.getValue());
        if (changed) {
            changedKeyList.add(SettingConstant.KEY_DEVICE_IMAGE_QUALITY);
        }

        //zoom rate
        ItemMeta<Float> zoomRateItem = (ItemMeta<Float>) zoomRateField.getSelectedItem();
        changed = setting.putFloatProperty(deviceId, SettingConstant.KEY_DEVICE_IMAGE_ZOOM_RATE, zoomRateItem.getValue());
        if (changed) {
            changedKeyList.add(SettingConstant.KEY_DEVICE_IMAGE_ZOOM_RATE);
        }

        setting.saveDeviceSetting(deviceId);

        if(changedKeyList.size() > 0) {
            Setting.getInstance().fireSettingResult(deviceId, changedKeyList.toArray(new String[0]));
        }

        dispose();
    }
}
