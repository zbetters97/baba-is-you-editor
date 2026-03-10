package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_On extends WordEntity {

    public static final String wordName = "WORD_ON";

    public WORD_On(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}