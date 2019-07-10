package com.cxplan.projection.ui.script;

import com.alee.laf.text.WebTextField;
import com.cxplan.projection.MonkeyConstant;
import com.cxplan.projection.ui.component.BaseDialog;
import com.cxplan.projection.ui.component.ItemMeta;
import com.cxplan.projection.ui.util.GUIUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Kenny
 * created on 2019/4/16
 */
public class KeycodeDialog extends BaseDialog {

    private JRadioButton builtItem;
    private JRadioButton elseItem;
    private JComboBox<ItemMeta> builtKeycodes;
    private WebTextField elseKeycode;
    private ButtonGroup itemGroup;

    private int keyCode = -1;
    private int result = -1;

    public KeycodeDialog(Window parent) {
        this(parent, -1);
    }
    public KeycodeDialog(Window parent, int keyCode) {
        super(parent, "Keycode Monkey Dialog", true);
        this.keyCode = keyCode;
        initView();
        setSize(300, 150);
    }

    public boolean isSelected() {
        return result > -1;
    }
    public int getSelectedKeyCode() {
        return result;
    }

    private void initView() {
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createMainPane(), BorderLayout.CENTER);

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addBtn = new JButton("Save");
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveResult();
            }
        });
        buttonPane.add(addBtn);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPane.add(cancelBtn);

        contentPane.add(buttonPane, BorderLayout.SOUTH);
    }

    private JPanel createMainPane() {
        double p = TableLayout.PREFERRED;
        double gap = 4;
        double[][] size = new double[][]{{gap, p, 10, p, gap},
                {
                        gap, p,
                        gap, p
                }};
        JPanel mainPane = new JPanel(new TableLayout(size));
        builtItem = new JRadioButton("Built-In");
        builtItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                builtKeycodes.setEnabled(true);
                elseKeycode.setEnabled(false);
            }
        });
        mainPane.add(builtItem, "1, 1");

        boolean isElseKeycode = keyCode != -1;
        builtKeycodes = new JComboBox<>();
        for (ItemMeta<Integer> im : MonkeyConstant.KEY_CODE_MAP.values()) {
            builtKeycodes.addItem(im);
            if (im.getValue() == keyCode) {
                isElseKeycode = false;
            }
        }
        builtKeycodes.setEnabled(!isElseKeycode);
        builtItem.setSelected(!isElseKeycode);
        mainPane.add(builtKeycodes, "3, 1");

        elseItem = new JRadioButton("Else");
        elseItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                builtKeycodes.setEnabled(false);
                elseKeycode.setEnabled(true);
            }
        });
        mainPane.add(elseItem, "1,3");

        elseKeycode = new WebTextField(6);
        elseKeycode.setEnabled(isElseKeycode);
        elseItem.setSelected(isElseKeycode);
        mainPane.add(elseKeycode, "3,3");

        itemGroup = new ButtonGroup();
        itemGroup.add(builtItem);
        itemGroup.add(elseItem);

        return mainPane;
    }

    private void saveResult() {
        if (itemGroup.isSelected(builtItem.getModel())) {
            result = ((ItemMeta<Integer>)builtKeycodes.getSelectedItem()).getValue();
        } else {
            try {
                result = Integer.parseInt(elseKeycode.getText().trim());
            } catch (Exception ex) {
                GUIUtil.showErrorMessageDialog("The keycode is illegal");
                elseKeycode.requestFocus();
                return;
            }
        }

        dispose();
    }

    public static void main(String[] args) {
        KeycodeDialog dialog = new KeycodeDialog(null);
        dialog.setVisible(true);
    }
}
