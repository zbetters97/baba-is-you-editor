package entity.object;

import application.GamePanel;
import entity.ObjectEntity;

public class OBJ_Key extends ObjectEntity {

    public static final String objName = "KEY";

    public OBJ_Key(GamePanel gp, int col, int row) {
        super(gp, col, row, objName);
    }
}