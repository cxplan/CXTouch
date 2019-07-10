package com.cxplan.projection.ui;

import java.awt.*;

/**
 * The expanded painter for device display component{@link DeviceDisplayPanel}.
 * You can paint extra something on device display component by implementing this interface.
 *
 * @author kenny
 * created on 2019-3-23
 */
public interface IDisplayPainter {

    void render(Graphics g);
}
