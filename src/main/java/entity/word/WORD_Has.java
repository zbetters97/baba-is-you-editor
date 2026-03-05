package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Has extends WordEntity {

    public static final String wordName = "WORD_HAS";

    public WORD_Has(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}