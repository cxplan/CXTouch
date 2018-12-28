package com.cxplan.projection.ui.component.monkey;

/**
 * Created on 2017/6/28.
 *
 * @author kenny
 */
public interface MonkeyInputListener {

    /**
     * Input a physical button.
     * @param keyCode reference for android.view.KeyEvent
     */
    void press(int keyCode);

    /**
     * type a char[0-9, a-z, A-Z], some common symbols.
     */
    void type(String ch);

    void touchDown(int x, int y);

    void touchUp(int x, int y);
    void touchMove(int x, int y);

    void scroll(int startx, int starty, int endx, int endy);
}
