package entity.object;

import application.GamePanel;
import entity.ObjectEntity;

public class OBJ_Rock extends ObjectEntity {

    public static final String objName = "ROCK";

    public OBJ_Rock(GamePanel gp, int col, int row) {
        super(gp, col, row, objName);
    }
}