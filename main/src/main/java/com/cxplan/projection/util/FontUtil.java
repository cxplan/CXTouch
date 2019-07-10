package com.cxplan.projection.util;

import java.awt.*;

/**
 * Created by kenny on 18-5-29.
 */
public class FontUtil
{
    private static Font font;

    static
    {
        if (SystemUtil.isWindow())
        {
            font = new Font("微软雅黑", Font.PLAIN, 14);
        }
        else
        {
            font = new Font("PingFang SC", Font.PLAIN, 14);
        }
    }

    public static Font getDefaultFont()
    {
        return getDefaultFont(14, Font.PLAIN);
    }

    public static Font getDefaultFont(int size)
    {
        return getDefaultFont(size, Font.PLAIN);
    }

    public static Font getDefaultFont(int size, int style)
    {
        return font.deriveFont(style, size);
        //return new Font("YaHei Consolas Hybrid",  style, size);
    }

}
