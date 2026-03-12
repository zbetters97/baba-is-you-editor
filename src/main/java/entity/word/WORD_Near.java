package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Near extends WordEntity {

    public static final String wordName = "WORD_NEAR";

    public WORD_Near(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}