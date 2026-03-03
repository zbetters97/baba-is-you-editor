package entity.object;

import application.GamePanel;
import entity.ObjectEntity;

public class OBJ_Flag extends ObjectEntity {

    public static final String objName = "FLAG";

    public OBJ_Flag(GamePanel gp, int col, int row) {
        super(gp, col, row, objName);
    }
}