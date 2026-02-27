package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Baba extends WordEntity {

    public static final String wordName = "WORD_BABA";

    public WORD_Baba(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}