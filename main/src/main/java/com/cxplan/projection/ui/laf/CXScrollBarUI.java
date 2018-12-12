package com.cxplan.projection.ui.laf;

import com.alee.laf.scroll.WebScrollBarUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * @author Kenny
 * created on 2018/11/6
 */
public class CXScrollBarUI extends WebScrollBarUI {

    public static ComponentUI createUI (final JComponent c )
    {
        return new CXScrollBarUI();
    }

    public CXScrollBarUI() {
    }

    @Override
    protected void installComponents ()
    {
        super.installComponents();
        decrButton.setMargin(new Insets(0, 0,0,0));
        incrButton.setMargin(new Insets(0, 0,0,0));
    }
}
