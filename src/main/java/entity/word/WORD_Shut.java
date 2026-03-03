package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Shut extends WordEntity {

    public static final String wordName = "WORD_SHUT";

    public WORD_Shut(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}