package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Is extends WordEntity {

    public static final String wordName = "WORD_IS";

    public WORD_Is(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}