package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Seeing extends WordEntity {

    public static final String wordName = "WORD_SEEING";

    public WORD_Seeing(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}