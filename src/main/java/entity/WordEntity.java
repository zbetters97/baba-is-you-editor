package entity;

import application.GamePanel;

public abstract class WordEntity extends Entity {

    public WordEntity(GamePanel gp, int col, int row, String name) {
        super(gp);

        this.name = name;

        worldX = col * gp.tileSize;
        worldY = row * gp.tileSize;

        collisionOn = false;
        properties.add(Property.PUSH);

        image = up1 = down1 = left1 = right1 = setupImage("/words/" + name.toLowerCase());
    }
}