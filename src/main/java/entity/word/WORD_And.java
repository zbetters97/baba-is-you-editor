package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_And extends WordEntity {

    public static final String wordName = "WORD_AND";

    public WORD_And(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}