package com.cxplan.projection.ui.laf;

import com.alee.global.StyleConstants;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButtonStyle;
import com.alee.laf.toolbar.WebToolBarStyle;
import com.alee.laf.tree.TreeSelectionStyle;
import com.alee.laf.tree.WebTreeStyle;
import com.alee.utils.swing.SwingLazyValue;
import com.cxplan.projection.util.FontUtil;
import com.jidesoft.plaf.LookAndFeelFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;
import java.util.Locale;

/**
 * @author KennyLiu
 * @created on 2018/11/27
 */
public class CXLookAndFeel extends WebLookAndFeel {

    /**
     * Installs WebLookAndFeel in one simple call.
     *
     * @return true if WebLookAndFeel was successfuly installed, false otherwise
     */
    public static boolean install ()
    {
        return install ( false );
    }

    /**
     * Installs WebLookAndFeel in one simple call and updates all existing components if requested.
     *
     * @param updateExistingComponents whether update all existing components or not
     * @return true if WebLookAndFeel was successfuly installed, false otherwise
     */
    public static boolean install ( final boolean updateExistingComponents )
    {
        try
        {
            CXLookAndFeel lookAndFeel = new CXLookAndFeel();
            Locale systemLocale = lookAndFeel.systemLocale;

            // Installing LookAndFeel
            UIManager.setLookAndFeel (lookAndFeel);

            //update locale
            updateLocale(systemLocale);

            // Updating already created components tree
            if ( updateExistingComponents )
            {
                updateAllComponentUIs ();
            }

            LookAndFeelFactory.installJideExtension();

            Font defaultFont = FontUtil.getDefaultFont(13);
            initGlobalFont(defaultFont);

            // LookAndFeel installed successfully
            return true;
        }
        catch ( final Throwable e )
        {
            // Printing exception
            e.printStackTrace ();

            // LookAndFeel installation failed
            return false;
        }
    }

    private static void initGlobalFont(final Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }

        LookAndFeelFactory.addUIDefaultsCustomizer(new LookAndFeelFactory.UIDefaultsCustomizer() {
            public void customize(UIDefaults defaults) {
                defaults.put("JideButton.font", font);
            }
        });
    }

    private static void updateLocale(Locale systemLocale) {
        String lang = systemLocale.getLanguage();
        if (!lang.equals("zh") &&
                !lang.equals("en")) {
            Locale.setDefault(new Locale("en", ""));
        } else {
            Locale.setDefault(systemLocale);
        }
    }

    private Locale systemLocale;

    public CXLookAndFeel() {
        systemLocale = Locale.getDefault();
    }

    @Override
    public void initialize() {
        WebButtonStyle.margin = new Insets(2, 8, 2, 8);
        WebToolBarStyle.borderColor = StyleConstants.LIGHT_ALPHA;
        WebTreeStyle.selectionStyle = TreeSelectionStyle.group;

        //font
        Font defaultFont = FontUtil.getDefaultFont(12);
        textPaneFont = defaultFont;
        editorPaneFont = defaultFont;
        globalControlFont = defaultFont;
        globalTooltipFont = defaultFont;
        globalAlertFont = defaultFont;
        globalMenuFont = defaultFont;
        globalAcceleratorFont = defaultFont;
        globalTitleFont = defaultFont;
        globalTextFont = defaultFont;

        super.initialize();
    }

    @Override
    protected void initClassDefaults(UIDefaults table)
    {
        Object defaultObject = table.get("FileChooserUI");
        super.initClassDefaults(table);
        if (defaultObject == null) {
            defaultObject = CXFileChooserUI.class.getCanonicalName();
        }
        table.put("FileChooserUI", defaultObject);
        table.put ( "ScrollBarUI", CXScrollBarUI.class.getCanonicalName() );
    }

    @Override
    protected void initComponentDefaults ( final UIDefaults table )
    {
        super.initComponentDefaults(table);
        // list focus border
        final Object listSelectedBorder =
                new SwingLazyValue( "javax.swing.plaf.BorderUIResource.LineBorderUIResource", new Object[]{ StyleConstants.focusColor } );
        table.put ( "List.focusCellHighlightBorder", listSelectedBorder );

        String lang = systemLocale.getLanguage();
        if (lang.equals("zh")) {
            //FileChooser top bar
            table.put("FileChooser.lookInLabelText", "查找");
            table.put("FileChooser.upFolderToolTipText", "向上一级");
            table.put("FileChooser.homeFolderToolTipText", "桌面");
            table.put("FileChooser.newFolderToolTipText", "新建文件夹");
            table.put("FileChooser.listViewButtonToolTipText", "列表");
            table.put("FileChooser.detailsViewButtonToolTipText", "详细信息");
            //FileChooser menu
            table.put("FileChooser.newFolderActionLabelText", "新建文件夹");
            table.put("FileChooser.refreshActionLabelText", "刷新");
            table.put("FileChooser.viewMenuLabelText", "视图");
            table.put("FileChooser.listViewActionLabelText", "列表");
            table.put("FileChooser.detailsViewActionLabelText", "详细信息");
        } else {
            //default value.
            table.put("FileChooser.lookInLabelText", "Find");
            table.put("FileChooser.upFolderToolTipText", "Up Folder");
            table.put("FileChooser.homeFolderToolTipText", "Home Folder");
            table.put("FileChooser.newFolderToolTipText", "New Folder");
            table.put("FileChooser.listViewButtonToolTipText", "List");
            table.put("FileChooser.detailsViewButtonToolTipText", "Details");
            //FileChooser menu
            table.put("FileChooser.newFolderActionLabelText", "New Folder");
            table.put("FileChooser.refreshActionLabelText", "Refresh");
            table.put("FileChooser.viewMenuLabelText", "View");
            table.put("FileChooser.listViewActionLabelText", "List");
            table.put("FileChooser.detailsViewActionLabelText", "Details");
        }
    }
}
