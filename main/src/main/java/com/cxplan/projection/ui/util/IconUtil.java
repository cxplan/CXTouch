package com.cxplan.projection.ui.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kenny liu on 2018/6/7.
 */
public class IconUtil
{
    private static Map<String, ImageIcon> iconCache;
    static {
        iconCache = new HashMap<>();
    }

    public static ImageIcon getIcon(String path)
    {
        return getIcon(path, -1, -1);
    }

    public static ImageIcon getIcon(String path, int width)
    {
        return getIcon(path, width, width);
    }

    public static ImageIcon getIcon(String path, int width, int height)
    {
        ImageIcon imageIcon = iconCache.get(path);
        if (imageIcon == null)
        {
            URL url = IconUtil.class.getResource(path);
            if (url == null)
            {
                return null;
            }

            imageIcon = new ImageIcon(url);

            if (width > 0 && height > 0 &&
                    (imageIcon.getIconWidth() != width || imageIcon.getIconHeight() != height))
            {
                imageIcon.setImage(imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }

            iconCache.put(path, imageIcon);
        }

        return imageIcon;
    }

    /**
     * return image with suitable dimension matched specified max width and height.
     */
    public static ImageIcon getIcon(ImageIcon imageIcon, int maxWidth, int maxHeight) {
        int width = imageIcon.getIconWidth();
        int height = imageIcon.getIconHeight();


        if (width <= maxWidth && height <= maxHeight) {
            return imageIcon;
        } else {
            int newWidth;
            int newHeight;
            if (width > maxWidth) {
                double rawRate = (double) width / height;
                double currentRate = (double) maxWidth / maxHeight;
                if (rawRate >= currentRate) {
                    newWidth = maxWidth;
                    newHeight = (int) (height * (double) maxWidth / width);
                } else {
                    newHeight = maxHeight;
                    newWidth = (int) (width * (double) maxHeight / height);
                }
            } else {
                double rawRate = (double) height / width;
                double currentRate = (double) maxHeight / maxWidth;
                if (rawRate >= currentRate) {
                    newHeight = maxHeight;
                    newWidth = (int) (width * (double) maxHeight / height);
                } else {
                    newWidth = maxWidth;
                    newHeight = (int) (height * (double) maxWidth / width);
                }
            }
            Image newImage = imageIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(newImage);
        }
    }

    public static double getIconScale(ImageIcon imageIcon, int maxWidth, int maxHeight) {
        int width = imageIcon.getIconWidth();
        int height = imageIcon.getIconHeight();
        if (width <= maxWidth && height <= maxHeight) {
            return 1.0D;
        } else {
            if (width > maxWidth) {
                double rawRate = (double) width / height;
                double currentRate = (double) maxWidth / maxHeight;
                if (rawRate >= currentRate) {
                    return (double) maxWidth / width;
                } else {
                    return (double) maxHeight / height;
                }
            } else {
                double rawRate = (double) height / width;
                double currentRate = (double) maxHeight / maxWidth;
                if (rawRate >= currentRate) {
                    return (double) maxHeight / height;
                } else {
                    return (double) maxWidth / width;
                }
            }
        }
    }
}
