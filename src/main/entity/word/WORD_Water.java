package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Water extends WordEntity {

    public static final String wordName = "WORD_WATER";

    public WORD_Water(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}