package application;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    /* GENERAL ATTRIBUTES */
    private boolean lock = true;

    /* BUTTON MAPPING */
    public final int btn_Start = KeyEvent.VK_SPACE;
    public final int btn_UP = KeyEvent.VK_UP;
    public final int btn_DOWN = KeyEvent.VK_DOWN;
    public final int btn_LEFT = KeyEvent.VK_LEFT;
    public final int btn_RIGHT = KeyEvent.VK_RIGHT;
    public final int btn_A = KeyEvent.VK_A;
    public final int btn_B = KeyEvent.VK_S;
    public final int btn_Y = KeyEvent.VK_D;

    /* CONFIG VALUES */
    public boolean upPressed, downPressed, leftPressed, rightPressed, startPressed, aPressed, bPressed, yPressed;

    /**
     * CONSTRUCTOR
     */
    public KeyHandler() {
    }

    /**
     * KEY TYPED
     * Unused method
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * KEY PRESSED
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // key pressed by user

        if (code == btn_UP) {
            upPressed = true;
        }
        if (code == btn_DOWN) {
            downPressed = true;
        }
        if (code == btn_LEFT) {
            leftPressed = true;
        }
        if (code == btn_RIGHT) {
            rightPressed = true;
        }
        if (code == btn_Start && lock) {
            startPressed = true;
            lock = false;
        }
        if (code == btn_A && lock) {
            aPressed = true;
            lock = false;
        }
        if (code == btn_B) {
            bPressed = true;
        }
        if (code == btn_Y) {
            yPressed = true;
            lock = false;
        }
    }

    /**
     * KEY RELEASED
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == btn_UP) {
            upPressed = false;
        }
        if (code == btn_DOWN) {
            downPressed = false;
        }
        if (code == btn_LEFT) {
            leftPressed = false;
        }
        if (code == btn_RIGHT) {
            rightPressed = false;
        }
        if (code == btn_Start) {
            startPressed = false;
            lock = true;
        }
        if (code == btn_A) {
            aPressed = false;
            lock = true;
        }
        if (code == btn_B) {
            bPressed = false;
        }
        if (code == btn_Y) {
            yPressed = false;
            lock = true;
        }
    }
}
