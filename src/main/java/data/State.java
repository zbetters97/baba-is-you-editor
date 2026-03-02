package data;

import application.GamePanel;

import java.awt.*;

public class State {
    public String name;
    public Point point;
    public GamePanel.Direction direction;
    public int ori, side;

    public State(String name, int x, int y, GamePanel.Direction direction, int ori, int side) {
        this.name = name;
        this.point = new Point(x, y);
        this.direction = direction;
        this.ori = ori;
        this.side = side;
    }
}
