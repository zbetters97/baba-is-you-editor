package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_You extends WordEntity {

    public static final String wordName = "WORD_YOU";

    public WORD_You(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}