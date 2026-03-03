package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Open extends WordEntity {

    public static final String wordName = "WORD_OPEN";

    public WORD_Open(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}