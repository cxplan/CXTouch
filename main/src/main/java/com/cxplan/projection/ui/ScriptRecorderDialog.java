package com.cxplan.projection.ui;

import com.alee.laf.button.WebButton;
import com.alee.laf.toolbar.WebToolBar;
import com.cxplan.projection.IApplication;
import com.cxplan.projection.script.ScriptRecorder;
import com.cxplan.projection.script.command.PressCommand;
import com.cxplan.projection.script.command.ScriptCommand;
import com.cxplan.projection.script.io.ScriptDeviceConnection;
import com.cxplan.projection.ui.component.BaseDialog;
import com.cxplan.projection.ui.component.RowHeaderList;
import com.cxplan.projection.ui.script.KeycodeDialog;
import com.cxplan.projection.ui.util.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * @author Kenny
 * created on 2019/4/5
 */
public class ScriptRecorderDialog extends BaseDialog {

    private IApplication application;
    private String deviceId;
    private DeviceImageFrame deviceFrame;
    private File scriptFile;

    //ui component
    private WebButton startScriptBtn;
    private WebButton stopScriptBtn;
    private WebButton keyCodeBtn;
    private WebButton playScriptBtn;
    private RowHeaderList<ScriptCommand> commandList;
    //popup menu for command list
    JPopupMenu popupMenu;

    private ScriptCommandListener commandListener;

    public ScriptRecorderDialog(DeviceImageFrame deviceFrame, final IApplication application, final String deviceId) {
        super(deviceFrame);
        setTitle("Script Recorder Dialog: " + deviceId);
        this.application = application;
        this.deviceId = deviceId;
        this.deviceFrame = deviceFrame;

        initView();
        createPopupMenu();

        setSize(500, 600);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ScriptDeviceConnection scriptConnection = application.getScriptConnection(deviceId);
                if (scriptConnection != null && scriptConnection.isRecording()) {
                    if (!GUIUtil.showConfirmDialog(ScriptRecorderDialog.this, "Recording is running, exit?")) {
                        return;
                    } else {
                        scriptConnection.stopRecord();
                    }
                }
                dispose();
            }
        });

        initData();
    }

    @Override
    public void dispose() {
        super.dispose();
        ScriptDeviceConnection scriptConnection = application.getScriptConnection(deviceId);
        if (scriptConnection != null) {
            scriptConnection.getScriptRecorder().removeRecorderListener(commandListener);
        }
    }

    private void initView() {

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        JComponent toolbar = createToolbar();
        contentPane.add(toolbar, BorderLayout.WEST);

        DefaultListModel model = new DefaultListModel();
        commandList = new RowHeaderList<>(model);
        commandList.setFixedCellHeight(24);
        contentPane.add(new JScrollPane(commandList), BorderLayout.CENTER);
        commandList.setShowRowNumber(true);

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        buttonPane.add(saveBtn);
        contentPane.add(buttonPane, BorderLayout.SOUTH);
    }

    private void initData() {
        //install listener
        commandListener = new ScriptCommandListener();
        ScriptDeviceConnection scriptConnection = getScriptConnection();
        scriptConnection.getScriptRecorder().addRecorderListener(commandListener);

    }

    private JPopupMenu createPopupMenu() {
        popupMenu = new JPopupMenu();

        JMenuItem editMenu = new JMenuItem("Edit");
        editMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editAction();
            }
        });
        popupMenu.add(editMenu);

        JMenuItem deleteMenu = new JMenuItem("Delete");
        deleteMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCommandAction();
            }
        });
        popupMenu.add(deleteMenu);

        commandList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupMenu.show((JComponent)e.getSource(), e.getX(), e.getY());
                }
            }
        });

        return popupMenu;
    }

    private WebToolBar createToolbar() {
        WebToolBar toolBar = new WebToolBar(JToolBar.VERTICAL);
        startScriptBtn = new WebButton("Start");
        startScriptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAction();
            }
        });
        toolBar.add(startScriptBtn);
        stopScriptBtn = new WebButton("Stop");
        stopScriptBtn.setEnabled(false);
        stopScriptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAction();
            }
        });
        toolBar.add(stopScriptBtn);
        keyCodeBtn = new WebButton("KeyCode");
        keyCodeBtn.setEnabled(false);
        keyCodeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeycodeDialog dialog = new KeycodeDialog(ScriptRecorderDialog.this);
                dialog.showCenterToOwner();
                if (dialog.isSelected()) {
                    PressCommand command = new PressCommand(dialog.getSelectedKeyCode());
                    ScriptDeviceConnection scriptConnection = getScriptConnection();
                    scriptConnection.getScriptRecorder().addEvent(command);
                }
            }
        });
        toolBar.add(keyCodeBtn);

        playScriptBtn = new WebButton("Play");
        playScriptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deviceFrame.playScript();
            }
        });
        toolBar.add(playScriptBtn);

        return toolBar;
    }

    //----------------action for buttons.
    private void startAction() {
        if (!deviceFrame.openScriptChannel()) {
            return;
        }
        clearCommandList();
        startScriptBtn.setEnabled(false);
        stopScriptBtn.setEnabled(true);
        keyCodeBtn.setEnabled(true);
        ScriptDeviceConnection scriptConnection = getScriptConnection();
        scriptConnection.getScriptRecorder().start();
    }

    private void stopAction() {
        startScriptBtn.setEnabled(true);
        stopScriptBtn.setEnabled(false);
        ScriptDeviceConnection scriptConnection = getScriptConnection();
        scriptConnection.getScriptRecorder().stop();
    }

    private void editAction() {
        ScriptCommand command = commandList.getSelectedValue();
        ScriptCommand result = command.toEdit(application, this);
        if (result != null) {
            commandList.repaint();
        }
    }

    private void deleteCommandAction() {
        int[] selectedIndex = commandList.getSelectedIndices();

        for (int i = selectedIndex[selectedIndex.length - 1]; i > -1; i--) {
            DefaultListModel model = (DefaultListModel) commandList.getModel();
            model.remove(selectedIndex[i]);
        }
    }

    private void clearCommandList() {
        DefaultListModel model = (DefaultListModel) commandList.getModel();
        model.clear();
    }

    private ScriptDeviceConnection getScriptConnection() {
        ScriptDeviceConnection scriptConnection = application.getScriptConnection(deviceId);
        if (scriptConnection == null) {
            throw new RuntimeException("The script connection doesn't exist");
        }
        return scriptConnection;
    }

    private void addCommand(ScriptCommand command) {
        DefaultListModel<ScriptCommand> model = (DefaultListModel<ScriptCommand>)commandList.getModel();
        model.addElement(command);
    }

    private class ScriptCommandListener implements ScriptRecorder.RecorderListener {

        @Override
        public void onCommandCreated(ScriptCommand command) {
            addCommand(command);
        }
    }
}
