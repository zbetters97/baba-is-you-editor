package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Skull extends WordEntity {

    public static final String wordName = "WORD_SKULL";

    public WORD_Skull(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}