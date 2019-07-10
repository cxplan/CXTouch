package com.cxplan.projection.script;

/**
 * @author kenny
 */
public enum CommandType {

    MOUSE_DOWN(1, "mouse_down"),
    MOUSE_UP(2, "mouse_up"),
    MOUSE_MOVE(3, "mouse_move"),
    TYPE(4, "type"),
    PRESS(5, "press"),
    SCROLL(6, "scroll"),
    TIME_GAP(7, "time_gap"),
    TOGGLE_SCREEN(8, "screen"),
    START_APP(100, "start_app");

    public static CommandType getType(int val) {
        switch (val) {
            case 1:
                return MOUSE_DOWN;
            case 2:
                return MOUSE_UP;
            case 3:
                return MOUSE_MOVE;
            case 4:
                return TYPE;
            case 5:
                return PRESS;
            case 6:
                return SCROLL;
            case 7:
                return TIME_GAP;
            case 100:
                return START_APP;

            default:
                throw new IllegalArgumentException("Illegal command type: " + val);
        }
    }

    private int val;
    private String name;

    CommandType(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return val;
    }
}
