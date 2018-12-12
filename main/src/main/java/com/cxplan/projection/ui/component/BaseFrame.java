package com.cxplan.projection.ui.component;


import com.cxplan.projection.ui.util.GUIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created on 2018/12/4.
 *
 * @author kenny
 */
public class BaseFrame extends JFrame{

    public BaseFrame() throws HeadlessException {
        init();
    }

    public BaseFrame(GraphicsConfiguration gc) {
        super(gc);
        init();
    }

    public BaseFrame(String title) throws HeadlessException {
        super(title);
        init();
    }

    public BaseFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        init();
    }

    protected void init() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        GUIUtil.setWindow(getClass(), this);
    }
    @Override
    public void dispose() {
        GUIUtil.removeWindow(this);
        super.dispose();
    }
}
