package com.cxplan.projection.ui.component.monkey;

import com.cxplan.projection.MonkeyConstant;
import com.cxplan.projection.ui.util.GUIUtil;
import com.cxplan.projection.util.StringUtil;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2018/6/28.
 *
 * @author kenny
 */
public class MonkeyCanvas extends Canvas implements KeyListener{

    /**
     * Physical Button supported for android is below.
     */
    private static final Map<Integer, Integer> keyMap = new HashMap<Integer, Integer>();
    static {
        keyMap.put(KeyEvent.VK_BACK_SPACE, MonkeyConstant.KEYCODE_BACK_SPACE);
        keyMap.put(KeyEvent.VK_DELETE, MonkeyConstant.KEYCODE_DELETE);
        keyMap.put(KeyEvent.VK_ENTER, MonkeyConstant.KEYCODE_ENTER);
        keyMap.put(KeyEvent.VK_LEFT, MonkeyConstant.KEYCODE_LEFT);
        keyMap.put(KeyEvent.VK_RIGHT, MonkeyConstant.KEYCODE_RIGHT);
        keyMap.put(KeyEvent.VK_UP, MonkeyConstant.KEYCODE_UP);
        keyMap.put(KeyEvent.VK_DOWN, MonkeyConstant.KEYCODE_DOWN);
    }

    private Point inputPosition;
    private MonkeyInputListener inputListener;
    private double scale;
    private long lastPressTime;

    public MonkeyCanvas(MonkeyInputListener inputListener) {
        initialize();
        this.inputListener = inputListener;
    }

    public MonkeyCanvas(GraphicsConfiguration config, MonkeyInputListener inputListener) {
        super(config);
        initialize();
        this.inputListener = inputListener;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    protected void initialize() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    return;
                }
                inputPosition = e.getPoint();
                lastPressTime = System.currentTimeMillis();
                Point p = new Point(e.getX(), e.getY());
                Point real = getRealPoint(p);
                inputListener.touchDown((int)real.getX(), (int)real.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    inputListener.press(MonkeyConstant.KEYCODE_BACK);
                    return;
                }

                Point p = new Point(e.getX(), e.getY());
                Point real = getRealPoint(p);
                inputListener.touchUp((int)real.getX(), (int)real.getY());
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int height = (int)(getHeight() / scale);
                int addition = height/4;
                inputListener.touchDown(10, height / 2);
                if (e.getWheelRotation() == 1) {//scroll up
                    inputListener.touchMove(10, height / 2 - addition);
                } else if (e.getWheelRotation() == -1) {//scroll down
                    inputListener.touchMove(10, height / 2 + addition);
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = new Point(e.getX(), e.getY());
                Point real = getRealPoint(p);
                inputListener.touchMove((int)real.getX(), (int)real.getY());
            }
        });

        addInputMethodListener(new DefaultInputMethodListener());
        addKeyListener(this);
        enableInputMethods(true);
    }

    @Override
    public void keyTyped(KeyEvent event) {
        char keyChar = event.getKeyChar();
        if (keyMap.containsKey((int)keyChar)) {
            return;
        } else {
            inputListener.type(keyChar + "");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getModifiers() == 0) {
            Integer keyCode = keyMap.get(e.getKeyCode());
            if (keyCode != null) {
                inputListener.press(keyCode);
            }
        } else if (e.getModifiers() == KeyEvent.ALT_MASK) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {//back: alt + delete/back
                inputListener.press(MonkeyConstant.KEYCODE_BACK);
            } else if (e.getKeyCode() == KeyEvent.VK_M) { // menu: alt + m
                inputListener.press(MonkeyConstant.KEYCODE_MENU);
            } else if (e.getKeyCode() == KeyEvent.VK_H) { //home : alt + h
                inputListener.press(MonkeyConstant.KEYCODE_HOME);
            }
        } else if (e.getModifiers() == KeyEvent.CTRL_MASK) {
            if (e.getKeyCode() == KeyEvent.VK_V) {//paste
                String text = GUIUtil.getClipboardText();
                if (text != null) {
                    inputListener.type(text);
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
        if (inputMethodRequestHandler == null) {
            inputMethodRequestHandler = new DefaultInputMethodRequests();
        }
        return inputMethodRequestHandler;
    }

    private Point getRealPoint(Point p) {
        int x = p.x;
        int y = p.y;
        int realX = (int) ((x) / scale);
        int realY = (int) ((y) / scale);
        return new Point(realX, realY);
    }

    private static final AttributedCharacterIterator.Attribute[] IM_ATTRIBUTES = { TextAttribute.INPUT_METHOD_HIGHLIGHT };
    private static final AttributedCharacterIterator EMPTY_TEXT = (new AttributedString("")).getIterator();

    private InputMethodRequests inputMethodRequestHandler;

    // The cache for text inputted by user.
    private StringBuilder messageText = new StringBuilder();

    private AttributedCharacterIterator composedText;
    private AttributedString composedTextString;

    class DefaultInputMethodListener implements InputMethodListener {

        @Override
        public void inputMethodTextChanged(InputMethodEvent e) {
            int committedCharacterCount = e.getCommittedCharacterCount();
            AttributedCharacterIterator text = e.getText();
            composedText = null;
            char c;
            if (text != null) {
                if (committedCharacterCount > 0) {
                    int toCopy = committedCharacterCount;
                    c = text.first();
                    StringBuilder sb = new StringBuilder();
                    while (toCopy-- > 0) {
                        sb.append(c);
                        c = text.next();
                    }
                    inputListener.type(sb.toString());
                }

                if (text.getEndIndex()
                        - (text.getBeginIndex() + committedCharacterCount) > 0) {
                    composedTextString = new AttributedString(text, text
                            .getBeginIndex()
                            + committedCharacterCount, text.getEndIndex(),
                            IM_ATTRIBUTES);
                    composedTextString.addAttribute(TextAttribute.FONT, getFont());
                    composedText = composedTextString.getIterator();
                    //TODO The code below need be extended
//                    System.out.print(composedText.first());
//                    while (composedText.getIndex() < composedText.getEndIndex()) {
//                        System.out.print(composedText.next());
//                    }
                }
            }
            e.consume();
        }

        @Override
        public void caretPositionChanged(InputMethodEvent event) {
            event.consume();
        }
    }

    class DefaultInputMethodRequests implements InputMethodRequests {

        @Override
        public Rectangle getTextLocation(TextHitInfo offset) {
            Rectangle rectangle;
            if (inputPosition == null) {
                Dimension dimension = getSize();
                inputPosition = new Point(dimension.width / 2, dimension.height / 2);
            }
            rectangle = new Rectangle(inputPosition, new Dimension(50, 20));
            Point location = getLocationOnScreen();
            rectangle.translate(location.x, location.y);
            return rectangle;
        }

        @Override
        public TextHitInfo getLocationOffset(int x, int y) {
            return null;
        }

        @Override
        public int getInsertPositionOffset() {
            return getCommittedTextLength();
        }

        @Override
        public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
            return getMessageText(beginIndex, endIndex);
        }

        public AttributedCharacterIterator getMessageText(int beginIndex,
                                                          int endIndex) {
            AttributedString string = new AttributedString(messageText.toString());
            return string.getIterator(null, beginIndex, endIndex);
        }

        @Override
        public int getCommittedTextLength() {
            return 0;
        }

        @Override
        public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
            return null;
        }

        @Override
        public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
            return EMPTY_TEXT;
        }
    }

}
