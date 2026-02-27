package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Flag extends WordEntity {

    public static final String wordName = "WORD_FLAG";

    public WORD_Flag(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}