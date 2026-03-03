package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Key extends WordEntity {

    public static final String wordName = "WORD_KEY";

    public WORD_Key(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}