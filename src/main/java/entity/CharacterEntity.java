package entity;

import application.GamePanel;

public abstract class CharacterEntity extends Entity {

    public CharacterEntity(GamePanel gp, int col, int row, String name) {
        super(gp);

        this.name = name;

        worldX = col * gp.tileSize;
        worldY = row * gp.tileSize;

        direction = GamePanel.Direction.RIGHT;

        getImages(name);
    }

    protected void getImages(String name) {
        String fileName = name.toLowerCase();

        image = setupImage("/characters/" + fileName);
        up1 = setupImage("/characters/" + fileName + "_up_1");
        up2 = setupImage("/characters/" + fileName + "_up_2");
        down1 = setupImage("/characters/" + fileName + "_down_1");
        down2 = setupImage("/characters/" + fileName + "_down_2");
        left1 = setupImage("/characters/" + fileName + "_left_1");
        left2 = setupImage("/characters/" + fileName + "_left_2");
        right1 = setupImage("/characters/" + fileName + "_right_1");
        right2 = setupImage("/characters/" + fileName + "_right_2");
    }
}