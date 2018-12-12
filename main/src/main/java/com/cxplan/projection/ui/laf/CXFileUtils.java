package com.cxplan.projection.ui.laf;

import com.alee.global.StyleConstants;
import com.alee.utils.FileUtils;
import com.alee.utils.ImageUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a set of utilities to work with files, file names and their extensions.
 * <p/>
 *
 * @author Kenny
 */
public class CXFileUtils
{
    /**
     * Cached file system view.
     */
    private static final FileSystemView fsv = FileSystemView.getFileSystemView ();

    /**
     * File extension icons cache lock.
     */
    private static final Object extensionIconsCacheLock = new Object ();

    /**
     * File extension icons cache.
     */
    private static final Map<String, ImageIcon> extensionIconsCache = new HashMap<String, ImageIcon> ();


    /**
     * Returns system file icon.
     *
     * @param file file to process
     * @return system file icon
     */
    public static ImageIcon getFileIcon ( final File file )
    {
        return getFileIcon ( file, false );
    }

    /**
     * Returns either large or small system file icon.
     *
     * @param file  file to process
     * @param large whether return large icon or not
     * @return either large or small system file icon
     */
    public static ImageIcon getFileIcon ( final File file, final boolean large )
    {
        return getStandartFileIcon ( file, large );
    }

    /**
     * Returns either large or small file icon from a standard icons set.
     *
     * @param file  file to process
     * @param large whether return large icon or not
     * @return either large or small file icon
     */
    public static ImageIcon getStandartFileIcon ( final File file, final boolean large )
    {
        return getStandartFileIcon ( file, large, true );
    }

    /**
     * Returns either large or small file icon from a standard icons set.
     *
     * @param file  file to process
     * @param large whether return large icon or not
     * @return either large or small file icon
     */
    public static ImageIcon getStandartFileIcon ( final File file, final boolean large, final boolean enabled )
    {
        if ( file == null )
        {
            return null;
        }

        // Retrieving required icon extension or type
        String extension;
        if ( !FileUtils.isDirectory ( file ) )
        {
            extension = FileUtils.getFileExtPart ( file.getName (), false ).trim ().toLowerCase ();
        }
        else
        {
            ImageIcon icon = (ImageIcon)fsv.getSystemIcon(file);
            if (icon != null) {
                return icon;
            }

            extension = "folder";
        }

        // Constructing icon cache key
        final float transparency = FileUtils.isHidden ( file ) ? 0.5f : 1f;
        final String key = getStandartFileIconCacheKey ( extension, large, transparency, enabled );

        // Retrieving icon
        final boolean contains;
        synchronized ( extensionIconsCacheLock )
        {
            contains = extensionIconsCache.containsKey ( key );
        }
        if ( contains )
        {
            synchronized ( extensionIconsCacheLock )
            {
                return extensionIconsCache.get ( key );
            }
        }
        else
        {
            ImageIcon icon = null;
            if (!extension.equals("folder")) {
                icon = (ImageIcon)fsv.getSystemIcon(file);
            }
            if (icon == null) {
                // Retrieving file type icon
                icon = FileUtils.getStandartFileIcon(large, extension, transparency);
                if (icon == null) {
                    // Simply use unknown file icon
                    icon = FileUtils.getStandartFileIcon(large, "file", transparency);
                }
            }

            // Caching the resulting icon
            if ( enabled )
            {
                // Cache enabled icon
                synchronized ( extensionIconsCacheLock )
                {
                    extensionIconsCache.put ( key, icon );
                }
            }
            else
            {
                // Cache enabled icon
                synchronized ( extensionIconsCacheLock )
                {
                    extensionIconsCache.put ( getStandartFileIconCacheKey ( extension, large, transparency, true ), icon );
                }

                // Cache disabled icon
                icon = ImageUtils.createDisabledCopy ( icon );
                synchronized ( extensionIconsCacheLock )
                {
                    extensionIconsCache.put ( key, icon );
                }
            }

            return icon;
        }
    }

    /**
     * Returns standart file icon cache key.
     *
     * @param extension    file extension or identifier
     * @param large        whether large icon used or not
     * @param transparency icon transparency
     * @param enabled      whether enabled icon or not
     * @return standart file icon cache key
     */
    private static String getStandartFileIconCacheKey ( final String extension, final boolean large, final float transparency,
                                                        final boolean enabled )
    {
        return extension + StyleConstants.SEPARATOR + large + StyleConstants.SEPARATOR + transparency + StyleConstants.SEPARATOR + enabled;
    }

}
