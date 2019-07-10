package com.cxplan.projection.ui.laf.tooltip;

import com.alee.laf.label.WebLabel;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.WebCustomTooltip;
import com.cxplan.projection.ui.laf.CXLookAndFeel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kenny
 * created on 2019/4/2
 */
public class CXTooltipManager {

    // Default settings
    private static int defaultDelay = 500;
    /**
     * Registers standart tooltip
     */

    public static WebCustomTooltip setTooltip (final Component component, final String tooltip )
    {
        return setTooltip ( component, tooltip, null );
    }

    public static WebCustomTooltip setTooltip (final Component component, final Icon icon, final String tooltip )
    {
        return setTooltip ( component, icon, tooltip, null );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final String tooltip, final TooltipWay tooltipWay )
    {
        return setTooltip ( component, tooltip, tooltipWay, defaultDelay );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final Icon icon, final String tooltip,
                                                final TooltipWay tooltipWay )
    {
        return setTooltip ( component, icon, tooltip, tooltipWay, defaultDelay );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final String tooltip, final TooltipWay tooltipWay,
                                                final int delay )
    {
        return setTooltip ( component, null, tooltip, tooltipWay, delay );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final Icon icon, final String tooltip,
                                                final TooltipWay tooltipWay, final int delay )
    {
        return setTooltip ( component, createDefaultComponent ( icon, tooltip ), tooltipWay, delay );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final JComponent tooltip )
    {
        return setTooltip ( component, tooltip, null );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final JComponent tooltip, final int delay )
    {
        return setTooltip ( component, tooltip, null, delay );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final JComponent tooltip, final TooltipWay tooltipWay )
    {
        return setTooltip ( component, tooltip, tooltipWay, defaultDelay );
    }

    public static WebCustomTooltip setTooltip ( final Component component, final JComponent tooltip, final TooltipWay tooltipWay,
                                                final int delay )
    {
        return TooltipManager.addTooltip ( component, tooltip, tooltipWay, delay );
    }

    public static WebLabel createDefaultComponent (final Icon icon, final String tooltip )
    {
        final WebLabel label = new WebLabel ( tooltip, icon );
        label.setStyleId ( "custom-tooltip-label" );
        label.setFont (CXLookAndFeel.toolTipFont );
        return label;
    }
}
