package entity.object;

import application.GamePanel;
import entity.Entity;

import java.awt.*;

public class OBJ_Flag extends Entity {

    public static final String objName = "FLAG";

    public OBJ_Flag(GamePanel gp, int x, int y) {
        super(gp);

        name = objName;

        worldX = x * gp.tileSize;
        worldY = y * gp.tileSize;

        image = up1 = down1 = left1 = right1 = setupImage("/objects/obj_" + objName.toLowerCase());
    }
}