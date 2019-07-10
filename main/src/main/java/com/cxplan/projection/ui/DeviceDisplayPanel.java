package com.cxplan.projection.ui;

import com.cxplan.projection.ui.component.monkey.MonkeyCanvas;
import com.cxplan.projection.ui.component.monkey.MonkeyInputListener;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_ProfileRGB;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

/**
 *
 * @author Kenny liu
 * 
 * Make sure OpenGL or XRender is enabled to get low latency, something like
 *      export _JAVA_OPTIONS=-Dsun.java2d.opengl=True
 *      export _JAVA_OPTIONS=-Dsun.java2d.xrender=True
 */
public class DeviceDisplayPanel extends JPanel {

    public static double getGamma(GraphicsDevice screen) {
        ColorSpace cs = screen.getDefaultConfiguration().getColorModel().getColorSpace();
        if (cs.isCS_sRGB()) {
            return 2.2;
        } else {
            try {
                return ((ICC_ProfileRGB)((ICC_ColorSpace)cs).getProfile()).getGamma(0);
            } catch (RuntimeException e) { }
        }
        return 0.0;
    }
    public static GraphicsDevice getScreenDevice(int screenNumber) throws Exception {
        GraphicsDevice[] screens = getScreenDevices();
        if (screenNumber >= screens.length) {
            throw new Exception("DisplayCanvas Error: Screen number " + screenNumber + " not found. " +
                                "There are only " + screens.length + " screens.");
        }
        return screens[screenNumber];//.getDefaultConfiguration();
    }
    public static GraphicsDevice[] getScreenDevices() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    }

    protected JComponent extComp;
    protected Canvas canvas = null;
    protected double displayScale = 1.0;
    protected double zoomRate = 1.0;
    protected double inverseGamma = 1.0;
    private Color color = null;
    private Image image = null;
    private BufferedImage buffer = null;
    private IDisplayPainter expandPainter;

    private boolean forceCanvasResize = false;

    public DeviceDisplayPanel(GraphicsConfiguration gc) {
        this(gc, 0.0, null);
    }
    public DeviceDisplayPanel(GraphicsConfiguration gc, MonkeyInputListener inputListener) {
        this(gc, 0.0, inputListener);
    }
    public DeviceDisplayPanel(GraphicsConfiguration gc, double gamma, MonkeyInputListener inputListener){
        this(gc, null, gamma, inputListener);
    }

    public DeviceDisplayPanel(GraphicsConfiguration gc, DisplayMode displayMode, MonkeyInputListener inputListener) {
        this(gc, displayMode, 0.0, inputListener);
    }
    public DeviceDisplayPanel(GraphicsConfiguration gc, DisplayMode displayMode, double gamma, MonkeyInputListener inputListener) {
        super();

//        double[][] size=new double[][]{{TableLayout.FILL},
//                {TableLayout.FILL,
//                        TableLayout.PREFERRED,
//                }};
        setLayout(new BorderLayout());

        doInit(gc, displayMode, gamma, inputListener);
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                forceCanvasResize = true;
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
    }

    public IDisplayPainter getExpandPainter() {
        return expandPainter;
    }

    public void setExpandPainter(IDisplayPainter expandPainter) {
        this.expandPainter = expandPainter;
        getCanvas().repaint();
    }

    private void doInit(final GraphicsConfiguration gc, final DisplayMode displayMode, final double gamma, MonkeyInputListener inputListener) {
        GraphicsDevice gd = gc.getDevice();
        DisplayMode d = gd.getDisplayMode(), d2 = null;
        if (displayMode != null && d != null) {
            int w = displayMode.getWidth();
            int h = displayMode.getHeight();
            int b = displayMode.getBitDepth();
            int r = displayMode.getRefreshRate();
            d2 = new DisplayMode(w > 0 ? w : d.getWidth(),    h > 0 ? h : d.getHeight(),
                                 b > 0 ? b : d.getBitDepth(), r > 0 ? r : d.getRefreshRate());
        }
        if (d2 != null && !d2.equals(d)) {
            gd.setDisplayMode(d2);
        }
        double g = gamma == 0.0 ? getGamma(gd) : gamma;
        inverseGamma = g == 0.0 ? 1.0 : 1.0/g;

        // Must be called after the fullscreen stuff, but before
        // getting our BufferStrategy or even creating our Canvas
        setVisible(true);

        initCanvas(displayMode, gamma, inputListener);

    }

    protected void initCanvas(DisplayMode displayMode, double gamma, MonkeyInputListener inputListener) {

        if (inputListener != null) {
            canvas = new MonkeyCanvas(inputListener) {
                @Override
                public void update(Graphics g) {
                    paint(g);
                }

                @Override
                public void paint(Graphics g) {
                    canvasPaint(g);
                }

            };
        } else {
            canvas = new Canvas() {
                @Override
                public void update(Graphics g) {
                    paint(g);
                }

                @Override
                public void paint(Graphics g) {
                    canvasPaint(g);
                }

            };
        }

        canvas.setSize(1,1); // mac bug

        add(canvas, BorderLayout.CENTER);
        canvas.setVisible(true);

    }

    private void canvasPaint(Graphics g) {
        // Calling BufferStrategy.show() here sometimes throws
        // NullPointerException or IllegalStateException,
        // but otherwise seems to work fine.
        try {
            if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
                return;
            }
            BufferStrategy strategy = canvas.getBufferStrategy();
            if (strategy == null) {
                canvas.createBufferStrategy(2);
                strategy = canvas.getBufferStrategy();
            }

            do {
                do {
                    g = strategy.getDrawGraphics();
                    if (color != null) {
                        g.setColor(color);
                        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    }
                    if (image != null) {
                        g.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(),this);
                    }
                    if (buffer != null) {
                        g.drawImage(buffer, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
                    }

                    if (expandPainter != null) {
                        expandPainter.render(g);
                    }

                    g.dispose();
                } while (strategy.contentsRestored());
                strategy.show();
            } while (strategy.contentsLost());
        } catch (NullPointerException e) {
        } catch (IllegalStateException e) { }

    }

    public void setExtComponent(JComponent comp) {
        if (extComp != null) {
            remove(extComp);
        }
        extComp = comp;
        add(extComp, BorderLayout.SOUTH);

    }

    public void showExtComponent() {
        if (extComp != null && extComp.getParent() == null) {
            add(extComp, BorderLayout.SOUTH);
        }
    }

    public void hideExtComponent() {
        if (extComp != null) {
            remove(extComp);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = new Dimension();
        Insets insets = getInsets();

        dim.width = insets.left + insets.right;
        dim.height = insets.top + insets.bottom;

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        dim.height += canvasHeight;
        dim.width += canvasWidth;

        if (extComp != null && getComponentZOrder(extComp) > -1) {
            Dimension extPreSize = extComp.getPreferredSize();
            dim.height += extPreSize.height;
        }

        return dim;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Dimension getCanvasSize() {
        return canvas.getSize();
    }
    public void setCanvasSize(final int width, final int height) {
        Dimension d = getCanvasSize();
        if (d.width == width && d.height == height) {
            return;
        }

        Runnable r = new Runnable() { public void run() {
            // There is apparently a bug in Java code for Linux, and what happens goes like this:
            // 1. Canvas gets resized, checks the visible area (has not changed) and updates
            // BufferStrategy with the same size. 2. pack() resizes the frame and changes
            // the visible area 3. We call Canvas.setSize() with different dimensions, to make
            // it check the visible area and reallocate the BufferStrategy almost correctly
            // 4. Finally, we resize the Canvas to the desired size... phew!
            canvas.setSize(width, height);

            validate();

            canvas.setSize(width+1, height+1);
            canvas.setSize(width, height);

            System.out.println("canvas size: " + width + "," + height);
        }};

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            try {
                EventQueue.invokeAndWait(r);
            } catch (Exception ex) { }
        }
    }

    public void setDeviceZoomRate(double zoomRate) {
        this.zoomRate = zoomRate;
        updateDeviceScale();
    }
    public double getCanvasScale() {
        return displayScale;
    }
    protected void setCanvasScale(double initialScale) {
        this.displayScale = initialScale;
        updateDeviceScale();
    }

    protected void updateDeviceScale() {
        if (canvas instanceof MonkeyCanvas) {
            ((MonkeyCanvas) canvas).setScale(displayScale * zoomRate);
        }
    }

    /**
     * Optimal the canvas size with specified image size.
     * If image size is out of the bound of canvas, the image will zoom out。
     *
     * @param imageWidth
     * @param imageHeight
     * @param isFill when image size is in the bound of canvas, this parameter determined whether
     *               the image should zoom in to fit canvas.
     */
    protected void updatePreferredScale(int imageWidth, int imageHeight, boolean isFill) {
        int width = getWidth();
        int height = getHeight();
        if (extComp != null && extComp.getParent() == this && extComp.isShowing()) {
            height -= extComp.getHeight();
        }

        if (imageWidth <= width && imageHeight <= height) {
            if (!isFill) {
                setCanvasSize(imageWidth, imageHeight);
            } else {
                double imageRate = (double)imageHeight / imageWidth;
                double canvasRate = (double) height / width;
                if (imageRate >= canvasRate) {
                    setCanvasScale((double)height / imageHeight);
                    setCanvasSize((int)(imageWidth * displayScale), height);
                } else {
                    setCanvasScale((double)width / imageWidth);
                    setCanvasSize(width, (int)(imageHeight * displayScale));
                }
            }
        } else {
            if (imageWidth > width) {
                double rawRate = (double)imageWidth / imageHeight;
                double currentRate = (double)width / height;
                if (rawRate >= currentRate) {
                    setCanvasScale((double)width/imageWidth);
                    setCanvasSize(width, (int)(imageHeight * displayScale));
                } else {
                    setCanvasScale((double)height / imageHeight);
                    setCanvasSize((int)(imageWidth * displayScale), height);
                }
            } else {
                double rawRate = (double)imageHeight / imageWidth;
                double currentRate = (double)height / width;
                if (rawRate >= currentRate) {
                    setCanvasScale((double)height/imageHeight);
                    setCanvasSize((int)(imageWidth * displayScale), height);
                } else {
                    setCanvasScale((double)width / imageWidth);
                    setCanvasSize(width, (int)(imageHeight * displayScale));
                }
            }
        }

    }

    public Graphics2D createGraphics() {
        if (buffer == null || buffer.getWidth() != canvas.getWidth() || buffer.getHeight() != canvas.getHeight()) {
            BufferedImage newbuffer = canvas.getGraphicsConfiguration().createCompatibleImage(
                    canvas.getWidth(), canvas.getHeight(), Transparency.TRANSLUCENT);
            if (buffer != null) {
                Graphics g = newbuffer.getGraphics();
                g.drawImage(buffer, 0, 0, null);
                g.dispose();
            }
            buffer = newbuffer;
        }
        return buffer.createGraphics();
    }
    public void releaseGraphics(Graphics2D g) {
        g.dispose();
        canvas.paint(null);
    }

    public void showColor(Color color) {
        this.color = color;
        this.image = null;
        canvas.paint(null);
    }

    // Java2D will do gamma correction for TYPE_CUSTOM BufferedImage, but
    // not for the standard types, so we need to do it manually.
    public void showImage(Image image) {
        showImage(image, -1, -1);
    }
    public void showImage(Image image, int defaultWidth, int defaultHeight) {
        if (image == null) {
            return;
        }

        //check whether the size of image is changed.
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if (imageWidth <= 0 && defaultWidth > 0) {
            imageWidth = defaultWidth;
        }
        if (imageHeight <= 0 && defaultHeight > 0) {
            imageHeight = defaultHeight;
        }
        if (forceCanvasResize || canvas.getWidth() != (int)(imageWidth * displayScale) || canvas.getHeight() != (int)(imageHeight * displayScale)) {
            if (forceCanvasResize) {
                forceCanvasResize = false;
            }
            updatePreferredScale(imageWidth, imageHeight, true);
        }

        this.color = null;
        if (imageWidth != canvas.getWidth() ||
                imageHeight != canvas.getHeight()) {
            this.image = image.getScaledInstance(canvas.getWidth(), canvas.getHeight(), Image.SCALE_SMOOTH);
        } else {
            this.image = image;
        }
        canvas.repaint();
    }

    public void refreshImage() {
        forceCanvasResize = true;
        showImage(this.image);
    }

}
