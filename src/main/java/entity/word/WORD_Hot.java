package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Hot extends WordEntity {

    public static final String wordName = "WORD_HOT";

    public WORD_Hot(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}