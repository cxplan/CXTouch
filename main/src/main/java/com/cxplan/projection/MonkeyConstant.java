package com.cxplan.projection;

/**
 * Created on 2017/5/24.
 *
 * @author kenny
 */
public class MonkeyConstant {

    /**
     * The monkey event.
     */
    public final static short EVENT_TYPE = 1;
    public final static short EVENT_PRESS = 2;
    public final static short EVENT_TOUCH_DOWN = 3;
    public final static short EVENT_TOUCH_MOVE = 4;
    public final static short EVENT_TOUCH_UP = 5;
    public final static short EVENT_TOUCH = 6;//down and then up
    public final static short EVENT_KEY_DOWN = 7;//press down
    public final static short EVENT_KEY_UP = 8;//press up
    public final static short EVENT_WAKE = 9;//wake screen.
    public final static short EVENT_SCROLL = 13;//scroll up the phone window
    public static final short MONKEY_SWITCH_INPUTER = 100;

    /**
     * Monkey keycode
     */
    public final static int KEYCODE_BACK_SPACE = 67;
    public final static int KEYCODE_DELETE = 112;
    public final static int KEYCODE_ENTER = 66;
    public final static int KEYCODE_LEFT = 21;
    public final static int KEYCODE_DOWN = 20;
    public final static int KEYCODE_UP = 19;
    public final static int KEYCODE_RIGHT = 22;
    /** Key code constant: Volume Up key.
     * Adjusts the speaker volume up. */
    public static final int KEYCODE_VOLUME_UP       = 24;
    /** Key code constant: Volume Down key.
     * Adjusts the speaker volume down. */
    public static final int KEYCODE_VOLUME_DOWN     = 25;

    public final static int KEYCODE_BACK = 4;
    public final static int KEYCODE_HOME = 3;
    public final static int KEYCODE_MENU = 82;
    public final static int KEYCODE_POWER = 26;

    /**
     * Rotation constant: 0 degree rotation (natural orientation)
     */
    public static final int ROTATION_0 = 0;
    /**
     * Rotation constant: 90 degree rotation.
     */
    public static final int ROTATION_90 = 1;
    /**
     * Rotation constant: 180 degree rotation.
     */
    public static final int ROTATION_180 = 2;
    /**
     * Rotation constant: 270 degree rotation.
     */
    public static final int ROTATION_270 = 3;

}
