package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Box extends WordEntity {

    public static final String wordName = "WORD_BOX";

    public WORD_Box(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}