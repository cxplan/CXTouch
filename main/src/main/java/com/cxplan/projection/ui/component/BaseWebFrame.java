package com.cxplan.projection.ui.component;


import com.alee.laf.rootpane.WebFrame;
import com.cxplan.projection.ui.util.GUIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created on 2018/12/4.
 *
 * @author kenny
 */
public class BaseWebFrame extends WebFrame {

    public BaseWebFrame() throws HeadlessException {
        init();
    }

    public BaseWebFrame(GraphicsConfiguration gc) {
        super(gc);
        init();
    }

    public BaseWebFrame(String title) throws HeadlessException {
        super(title);
        init();
    }

    public BaseWebFrame(String title, GraphicsConfiguration gc) {
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
