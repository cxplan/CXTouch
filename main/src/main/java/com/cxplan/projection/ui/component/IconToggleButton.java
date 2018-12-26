package com.cxplan.projection.ui.component;

import com.alee.global.StyleConstants;
import com.alee.laf.button.WebButton;
import com.alee.laf.button.WebToggleButton;

import javax.swing.*;
import java.awt.*;

/**
 * Toggle Button implement only with Icon.
 *
 * @author kenny
 */
public class IconToggleButton extends WebToggleButton {

	private static final long serialVersionUID = 1L;

    public IconToggleButton(Icon icon) {
        super(icon);
        setRolloverDecoratedOnly(true);
        Dimension dim = getIconButtonSize();
        if (dim != null) {
            this.setPreferredSize(dim);
        }
        setMargin(1);
        setRound ( StyleConstants.smallRound );
        setShadeWidth ( StyleConstants.shadeWidth );
        setInnerShadeWidth ( StyleConstants.innerShadeWidth );
        setLeftRightSpacing ( 0 );
        setUndecorated ( StyleConstants.undecorated );
        setDrawFocus ( true );
    }

    protected Dimension getIconButtonSize() {
        return null;
    }
}
