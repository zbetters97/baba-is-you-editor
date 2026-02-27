package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Stop extends WordEntity {

    public static final String wordName = "WORD_STOP";

    public WORD_Stop(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}