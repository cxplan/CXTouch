package com.cxplan.projection.ui.laf;

import com.alee.laf.filechooser.WebFileChooserPanel;
import com.alee.utils.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalFileChooserUI;
import java.io.File;

/**
 * @author Kenny
 * created on 2018/11/8
 */
public class CXFileChooserUI extends MetalFileChooserUI {

    public static ComponentUI createUI(JComponent c) {
        return new CXFileChooserUI((JFileChooser) c);
    }
    /**
     * Special FileView for file chooser.
     */
    private CXFileView fileView;

    public CXFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }

    @Override
    public void installUI ( final JComponent c ) {
        fileView = new CXFileView();
        ((JFileChooser)c).setFileView(fileView);
        super.installUI(c);
    }

    @Override
    public void installComponents(JFileChooser fc) {
        upFolderIcon = WebFileChooserPanel.FOLDER_UP_ICON;
        detailsViewIcon  = WebFileChooserPanel.VIEW_TABLE_ICON;
        listViewIcon     = WebFileChooserPanel.VIEW_TILES_ICON;
        homeFolderIcon = WebFileChooserPanel.FOLDER_HOME_ICON;
        newFolderIcon = WebFileChooserPanel.FOLDER_NEW_ICON;
        super.installComponents(fc);
    }
    @Override
    public FileView getFileView ( final JFileChooser fc )
    {
        return fileView;
    }

    /**
     * @param fileView
     */
    public void setFileView ( final CXFileView fileView )
    {
        this.fileView = fileView;
    }

    /**
     * Special FileView for file chooser.
     */
    protected class CXFileView extends FileView
    {
        /**
         * Constructs new WebFileView instance.
         */
        public CXFileView ()
        {
            super ();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName ( final File f )
        {
            return FileUtils.getDisplayFileName ( f );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription ( final File f )
        {
            return getTypeDescription ( f );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getTypeDescription ( final File f )
        {
            return FileUtils.getFileTypeDescription ( f );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon ( final File f )
        {
            return CXFileUtils.getFileIcon ( f );
        }
    }
}
