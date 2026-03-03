package entity;

import application.GamePanel;

public abstract class ObjectEntity extends Entity {

    public ObjectEntity(GamePanel gp, int col, int row, String name) {
        super(gp);

        this.name = name;

        worldX = col * gp.tileSize;
        worldY = row * gp.tileSize;

        image = up1 = down1 = left1 = right1 = setupImage("/objects/" + name.toLowerCase());
    }
}