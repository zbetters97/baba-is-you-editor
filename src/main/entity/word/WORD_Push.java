package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Push extends WordEntity {

    public static final String wordName = "WORD_PUSH";

    public WORD_Push(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}