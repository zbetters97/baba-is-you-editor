package data;

import application.GamePanel;

import java.awt.*;

public class State {
    public String name;
    public Point point;
    public GamePanel.Direction direction;

    public State(String name, int x, int y, GamePanel.Direction direction) {
        this.name = name;
        this.point = new Point(x, y);
        this.direction = direction;
    }
}
