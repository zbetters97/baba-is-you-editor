package entity.character;

import application.GamePanel;
import entity.Entity;

import static application.GamePanel.Direction.RIGHT;

public class CHR_Keke extends Entity {

    public static final String chrName = "KEKE";

    public CHR_Keke(GamePanel gp, int col, int row) {
        super(gp);

        name = chrName;

        worldX = col * gp.tileSize;
        worldY = row * gp.tileSize;

        direction = RIGHT;
    }

    protected void getImages() {
        image = setupImage("/characters/keke");
        up1 = setupImage("/characters/keke_up_1");
        up2 = setupImage("/characters/keke_up_2");
        down1 = setupImage("/characters/keke_down_1");
        down2 = setupImage("/characters/keke_down_2");
        left1 = setupImage("/characters/keke_left_1");
        left2 = setupImage("/characters/keke_left_2");
        right1 = setupImage("/characters/keke_right_1");
        right2 = setupImage("/characters/keke_right_2");
    }
}
