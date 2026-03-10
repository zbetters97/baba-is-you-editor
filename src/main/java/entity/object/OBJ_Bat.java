package entity.object;

import application.GamePanel;
import entity.ObjectEntity;

public class OBJ_Bat extends ObjectEntity {

    public static final String objName = "BAT";

    public OBJ_Bat(GamePanel gp, int col, int row) {
        super(gp, col, row, objName);
    }
}