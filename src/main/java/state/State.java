package state;

import application.GamePanel;

import java.awt.*;

public class State {
    public String name;
    public Point point, previousPoint;
    public GamePanel.Direction direction;
    public int ori, side;

    public State(String name, Point point, Point previousPoint, GamePanel.Direction direction, int ori, int side) {
        this.name = name;
        this.point = new Point(point);
        this.previousPoint = new Point(previousPoint);
        this.direction = direction;
        this.ori = ori;
        this.side = side;
    }
}
