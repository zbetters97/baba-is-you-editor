package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Door extends WordEntity {

    public static final String wordName = "WORD_DOOR";

    public WORD_Door(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}