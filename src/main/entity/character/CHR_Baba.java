package entity.character;

import application.GamePanel;
import entity.Entity;

import static application.GamePanel.Direction.RIGHT;

public class CHR_Baba extends Entity {

    public static final String chrName = "BABA";

    public CHR_Baba(GamePanel gp, int col, int row) {
        super(gp);

        name = chrName;

        worldX = col * gp.tileSize;
        worldY = row * gp.tileSize;

        direction = RIGHT;
    }

    protected void getImages() {
        image = setupImage("/characters/baba");
        up1 = setupImage("/characters/baba_up_1");
        up2 = setupImage("/characters/baba_up_2");
        down1 = setupImage("/characters/baba_down_1");
        down2 = setupImage("/characters/baba_down_2");
        left1 = setupImage("/characters/baba_left_1");
        left2 = setupImage("/characters/baba_left_2");
        right1 = setupImage("/characters/baba_right_1");
        right2 = setupImage("/characters/baba_right_2");
    }
}
