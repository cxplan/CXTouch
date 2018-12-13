package com.cxplan.projection.ui.component;

import com.alee.global.StyleConstants;
import com.alee.laf.button.WebButton;

import javax.swing.*;
import java.awt.*;

/**
 * Button implement only with Icon.
 *
 * @author kenny
 */
public class IconButton extends WebButton {

	private static final long serialVersionUID = 1L;
	
    public IconButton(Icon icon) {
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
