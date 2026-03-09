package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Shift extends WordEntity {

    public static final String wordName = "WORD_SHIFT";

    public WORD_Shift(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}