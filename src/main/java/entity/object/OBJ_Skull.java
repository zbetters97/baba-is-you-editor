package entity.object;

import application.GamePanel;
import entity.ObjectEntity;

public class OBJ_Skull extends ObjectEntity {

    public static final String objName = "SKULL";

    public OBJ_Skull(GamePanel gp, int col, int row) {
        super(gp, col, row, objName);
    }
}