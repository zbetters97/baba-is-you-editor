package entity.object;

import application.GamePanel;
import entity.ObjectEntity;

public class OBJ_Box extends ObjectEntity {

    public static final String objName = "BOX";

    public OBJ_Box(GamePanel gp, int col, int row) {
        super(gp, col, row, objName);
    }
}