package com.cxplan.projection.ui.component;

import com.cxplan.projection.ui.util.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created on 2018/6/16.
 *
 * @author kenny
 */
public class BaseDialog extends JDialog {

    public BaseDialog() {
        init();
    }

    public BaseDialog(Frame owner) {
        super(owner);
        init();
    }

    public BaseDialog(Frame owner, boolean modal) {
        super(owner, modal);
        init();
    }

    public BaseDialog(Frame owner, String title) {
        super(owner, title);
        init();
    }

    public BaseDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        init();
    }

    public BaseDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        init();
    }

    public BaseDialog(Dialog owner) {
        super(owner);
        init();
    }

    public BaseDialog(Dialog owner, boolean modal) {
        super(owner, modal);
        init();
    }

    public BaseDialog(Dialog owner, String title) {
        super(owner, title);
        init();
    }

    public BaseDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        init();
    }

    public BaseDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        init();
    }

    public BaseDialog(Window owner) {
        super(owner);
        init();
    }

    public BaseDialog(Window owner, ModalityType modalityType) {
        super(owner, modalityType);
        init();
    }

    public BaseDialog(Window owner, String title) {
        super(owner, title);
        init();
    }

    public BaseDialog(Window owner, String title, boolean modal) {
        this(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
    }

    public BaseDialog(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
        init();
    }

    public BaseDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
        init();
    }

    protected void init() {
        initUI();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        GUIUtil.setWindow(getClass(), this);
    }

    protected void initUI() {
        JRootPane rp=getRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        InputMap inputMap = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        rp.getActionMap().put("ESCAPE", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void dispose() {
        GUIUtil.removeWindow(this);
        super.dispose();
    }
}
