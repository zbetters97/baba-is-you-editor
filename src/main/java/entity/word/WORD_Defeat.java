package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Defeat extends WordEntity {

    public static final String wordName = "WORD_DEFEAT";

    public WORD_Defeat(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}