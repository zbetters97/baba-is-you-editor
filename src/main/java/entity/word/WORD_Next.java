package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Next extends WordEntity {

    public static final String wordName = "WORD_NEXT";

    public WORD_Next(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}