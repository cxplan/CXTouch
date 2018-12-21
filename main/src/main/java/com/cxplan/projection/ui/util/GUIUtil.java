/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.ui.util;

import com.cxplan.projection.ui.component.WindowMeta;
import com.cxplan.projection.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2017/5/6.
 *
 * @author kenny
 */
public class GUIUtil {

    private static final Logger logger = LoggerFactory.getLogger(GUIUtil.class);

    public static JFrame mainFrame;
    public static Image appLogo;

    private static Map<Class<? extends Window>, Set<Window>> windowMap = new ConcurrentHashMap<>();
    public static FileFilter[] imageFilters;
    static {
        imageFilters = new FileFilter[6];
        imageFilters[0] = new ImageFileFilter(new String[]{"jpg", "png", "jpeg", "bmp", "gif"});
        imageFilters[1] = new ImageFileFilter("png");
        imageFilters[2] = new ImageFileFilter("jpeg");
        imageFilters[3] = new ImageFileFilter("bmp");
        imageFilters[4] = new ImageFileFilter("gif");
        imageFilters[5] = new ImageFileFilter("jpg");
    }

    public static <T extends Window> T getWindow(Class<? extends Window> clazz) {
        WindowMeta wm = clazz.getAnnotation(WindowMeta.class);
        if (wm != null && wm.instance() == WindowMeta.MULTI) {
            throw new RuntimeException("The window is allowed more than one instance.");
        }
        Set<Window> windowSet = windowMap.get(clazz);
        if (windowSet == null || windowSet.size() == 0) {
            return null;
        } else {
            for (Window window : windowSet) {
                return (T)window;
            }
        }

        return null;
    }
    public static void setWindow(Class<? extends Window> clazz, Window window) {
        WindowMeta wm = window.getClass().getAnnotation(WindowMeta.class);
        boolean multiInstance = false;
        if (wm != null) {
            multiInstance = wm.instance() == WindowMeta.MULTI;
        }

        Set<Window> windowSet = windowMap.get(clazz);
        if (windowSet == null) {
            synchronized (windowMap) {
                windowSet = new HashSet<>();
                windowMap.put(clazz, windowSet);
            }
        }

        if (multiInstance) {
            windowSet.add(window);
        } else {
            windowSet.clear();
            windowSet.add(window);
        }
    }
    public static boolean removeWindow(Window window) {
        Set<Window> windowSet = windowMap.get(window.getClass());
        if (windowSet == null) {
            return false;
        }
        return windowSet.remove(window);
    }

    public static void resetWindows() {
        Iterator<Map.Entry<Class<? extends Window>, Set<Window>>> it = windowMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Class<? extends Window>, Set<Window>> entry = it.next();
            Set<Window> windowSet = entry.getValue();
            boolean isMain = false;
            if (windowSet != null) {
                for (Window window : windowSet) {
                    if (window == mainFrame) {
                        window.setVisible(false);
                        isMain = true;
                    } else {
                        window.dispose();
                    }
                }
            }
            if (!isMain) {
                it.remove();
                if (windowSet != null) {
                    windowSet.clear();
                }
            }
        }
        logger.info("The windowMap size: {}", windowMap.size());
    }
    /**
     * Place frame container at the center of owner container.
     *
     * @param owner the referred container.
     * @param frame the container need be modify position.
     */
    public static void centerFrameToFrame(Container owner, Container frame) {
        if (owner == null) {
            Dimension rect = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setBounds((int) ((rect.getWidth() - frame.getWidth()) / 2),
                    (int) ((rect.getHeight() - frame.getHeight()) / 2), frame
                            .getWidth(), frame.getHeight());

            return;
        }
        Rectangle rect = owner.getBounds();
        frame
                .setBounds((int) (rect.getX() + (rect.getWidth() - frame
                        .getWidth()) / 2), (int) (rect.getY() + (rect
                        .getHeight() - frame.getHeight()) / 2), frame
                        .getWidth(), frame.getHeight());
    }
    public static void centerToOwnerWindow(Window frame)
    {
        if(frame==null)
            return;
        Container owner=frame.getOwner();
        if(owner==null)
            return;
        centerFrameToFrame(owner,frame);
    }

    public static void showWindowCenterToOwner(Window frame) {
        centerToOwnerWindow(frame);
        frame.setVisible(true);
    }


    public static void showErrorMessageDialog(String message) {
        showMessageDialog(findLikelyOwnerWindow(), message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }
    public static void showErrorMessageDialog(String message, String title) {
        showMessageDialog(findLikelyOwnerWindow(), message, title, JOptionPane.ERROR_MESSAGE);
    }
    public static void showInfoMessageDialog(String message) {
        showInfoMessageDialog(message, "INFO");
    }
    public static void showInfoMessageDialog(String message, String title) {
        showMessageDialog(findLikelyOwnerWindow(), message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    public static void showMessageDialog(final Component parent, final String message, final String title, final int messageType) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parent, message, title, messageType);
            }
        });
    }
    public static boolean showConfirmDialog(String message) {
        return showConfirmDialog(findLikelyOwnerWindow(), message);
    }
    public static boolean showConfirmDialog(Component parent, String message) {
        int ret = JOptionPane.showConfirmDialog(parent, message, "Confirm Dialog", JOptionPane.YES_NO_OPTION);
        return ret == JOptionPane.YES_OPTION;
    }

    /**
     * Return the string content in clipboard
     */
    public static String getClipboardText() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(null);
        if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            String text = null;//从数据中获取文本值
            try {
                text = (String) content.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e1) {
                logger.error(e1.getMessage(), e1);
            }
            return text;
        } else {
            return null;
        }
    }
    public static String showInputDialog(String message) {
        return showInputDialog(message, null);
    }
    /**
     * Null indicates user cancel input.
     */
    public static String showInputDialog(String message, String initValue) {
        String value;
        if (initValue != null) {
            value = JOptionPane.showInputDialog(findLikelyOwnerWindow(), message, initValue);
        } else {
            value = JOptionPane.showInputDialog(findLikelyOwnerWindow(), message);
        }
        if (value == null) {
            return null;
        }
        value = value.trim();
        if(StringUtil.isEmpty(value)) {
            return showInputDialog(message, initValue);
        }
        return value;
    }
    /**
     * Returns the focused Window, if the focused Window is in the same context
     * as the calling thread. The focused Window is the Window that is or
     * contains the focus owner.
     *
     * @return the focused Window
     */
    public static Window findLikelyOwnerWindow() {
        Window result = KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .getFocusedWindow();

        return result;
    }

    public static <T extends Container> T getTopParent(Container com) {
        return (T) getUpParent(com, Window.class);
    }

    /**
     * @see #getUpParent(Container, Class, boolean)
     */
    public static <T> T getUpParent(Container com, Class<T> parent) {
        return getUpParent(com, parent, true);
    }

    /**
     * Retrieve the parent component matched specified type.
     *
     * @param com current component.
     * @param parent The class type of  parent component
     * @param isMustVisible specify whether returned parent component must be visible.
     * @return the parent component object.
     */
    public static <T> T getUpParent(Container com, Class<T> parent,
                                                      boolean isMustVisible) {
        if (com == null || parent == null)
            return null;
        if (parent == com.getClass())
            return (T)com;
        Container con = com.getParent();
        for (; con != null && !parent.isAssignableFrom(con.getClass()); con = con
                .getParent());
        if (con != null && !con.isVisible() && isMustVisible)
        {
            con = (Container) getUpParent(con, parent, isMustVisible);
        }
        return (T)con;
    }

    /**
     * Return the text content on clipboard.
     */
    public static String getClipText() {
        Transferable transfer = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (!transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return null;
        }
        String dataObj;
        try {
            dataObj = (String) transfer.getTransferData(DataFlavor.stringFlavor);
            return dataObj;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    /**
     * Return the file list on clipboard.
     */
    public static java.util.List<File> getClipFile() {
        Transferable transfer = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (!transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return null;
        }
        java.util.List<File> fileList;
        try {
            fileList = (java.util.List<File>) transfer.getTransferData(DataFlavor.javaFileListFlavor);
            return fileList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String lastSelectedDir;

    /**
     * open a image, single file only
     */
    public static File selectImageFile(Container con) {
        File[] ret = selectImageFile(con, false);
        if (ret == null) {
            return null;
        } else {
            lastSelectedDir = ret[0].getParent();
            return ret[0];
        }
    }
    public static File[] selectImageFile(Container con, boolean isMulti) {
        File[] ret = selectFileByFilter(con, imageFilters[0], imageFilters, null,
                lastSelectedDir == null ? "" : lastSelectedDir, isMulti, true, false);
        if (ret != null && ret.length > 0) {
            lastSelectedDir = ret[0].getParent();
        }
        return ret;
    }

    public static File[] selectFile(Container container, String selectedFile, boolean isMultiSelect) {
        File[] ret = selectFileByFilter(container, null, null, selectedFile != null ? new String[]{selectedFile} : null,
                lastSelectedDir == null ? "" : lastSelectedDir, isMultiSelect, false, false);
        if (ret == null) {
            return null;
        } else {
            lastSelectedDir = ret[0].getParent();
            return ret;
        }
    }

    public static File[] selectFile(Container container, boolean isMultiSelect,
                                    FileFilter initialFilter, FileFilter filters[], boolean isOpen) {
        File[] ret = selectFileByFilter(container, initialFilter, filters, null,
                lastSelectedDir == null ? "" : lastSelectedDir, isMultiSelect, isOpen, false);
        if (ret == null) {
            return null;
        } else {
            lastSelectedDir = ret[0].getParent();
            return ret;
        }
    }

    public static File[] selectDirectory(Container container, String currentDir, boolean isMultiSelect, boolean isOpen) {
        if (currentDir == null) {
            currentDir = lastSelectedDir;
        }
        JFileChooser fc = new JFileChooser(currentDir);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(isMultiSelect);

        int select = isOpen?fc.showOpenDialog(container):fc.showSaveDialog(container);
        if (select == JFileChooser.APPROVE_OPTION) {
            File[] files;
            if (fc.isMultiSelectionEnabled()) {
                files = fc.getSelectedFiles();
                lastSelectedDir = files[0].getParent();
                return files;
            } else {
                files = new File[]{fc.getSelectedFile()};
                lastSelectedDir = files[0].getParent();
                return files;
            }
        } else {
            return null;
        }
    }

    public static File[] selectFileByFilter(Container con, FileFilter initialFilter, FileFilter filters[], String[] selectedFiles,
                                            String currentDir, boolean isMutiSelectable, boolean isOpen, boolean isPromptOnExist) {
        JFileChooser fc = new JFileChooser(currentDir);

        if(filters!=null)
        {
            for(int i=0;i<filters.length;i++)
            {
                if(filters[i]==null)
                    continue;
                fc.addChoosableFileFilter(filters[i]);
            }
        }
        if (selectedFiles != null && selectedFiles.length > 0) {
            File[] tmpFiles = new File[selectedFiles.length];
            for (int i = 0; i < tmpFiles.length; i++) {
                tmpFiles[i] = new File(currentDir, selectedFiles[i]);
            }
            fc.setSelectedFiles(tmpFiles);
        }
        if(initialFilter!=null)
            fc.setFileFilter(initialFilter);
        fc.setMultiSelectionEnabled(isMutiSelectable);
        int select = isOpen?fc.showOpenDialog(con):fc.showSaveDialog(con);
        if (select == JFileChooser.APPROVE_OPTION) {
            File[] tmp =null;
            if(isMutiSelectable)
                tmp=fc.getSelectedFiles();
            else
            {
                tmp=new File[]{fc.getSelectedFile()};
            }
            if (tmp != null&&tmp.length==1) {//do only  when one file is selected .
                if (isPromptOnExist&&!isOpen&&tmp[0].exists()) {
                    int result = JOptionPane.showConfirmDialog(con,
                            "The file exists already, overwrite it?",
                            "Confirm!", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.NO_OPTION)
                        return selectFileByFilter(con,initialFilter ,filters, selectedFiles,currentDir,isMutiSelectable,isOpen,isPromptOnExist);
                }
            }
            return tmp;
        } else
            return null;
    }

    private static class ImageFileFilter extends FileFilter {

        private String[] name;
        public ImageFileFilter(String name) {
            this.name = new String[1];
            this.name[0] = name;
        }

        public ImageFileFilter(String[] name) {
            this.name = name;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String lowName = f.getName().toLowerCase();
            for (String tmp : name) {
                if (lowName.endsWith("." + tmp)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "image:" + getNameString();
        }
        private String getNameString() {
            if (name.length == 1) {
                return name[0];
            }
            StringBuilder sb = new StringBuilder();
            for (String tmp : name) {
                sb.append(tmp).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }

}
