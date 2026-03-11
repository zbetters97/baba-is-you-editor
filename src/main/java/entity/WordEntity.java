package entity;

import application.GamePanel;

import java.awt.*;

public abstract class WordEntity extends Entity {

    public WordEntity(GamePanel gp, int col, int row, String name) {
        super(gp);

        this.point = new Point(col * gp.tileSize, row * gp.tileSize);
        setPreviousPoint(point);
        this.name = name;

        image = up1 = down1 = left1 = right1 = setupImage("/words/" + name.toLowerCase());
    }
}