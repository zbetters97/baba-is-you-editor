package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Bat extends WordEntity {

    public static final String wordName = "WORD_BAT";

    public WORD_Bat(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}