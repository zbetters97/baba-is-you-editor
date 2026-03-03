package entity.object;

import application.GamePanel;
import entity.ObjectEntity;

public class OBJ_Door extends ObjectEntity {

    public static final String objName = "DOOR";

    public OBJ_Door(GamePanel gp, int col, int row) {
        super(gp, col, row, objName);
    }
}