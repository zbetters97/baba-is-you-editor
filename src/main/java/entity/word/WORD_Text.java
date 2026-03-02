package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Text extends WordEntity {

    public static final String wordName = "WORD_TEXT";

    public WORD_Text(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}