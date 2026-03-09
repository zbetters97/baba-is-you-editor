package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Belt extends WordEntity {

    public static final String wordName = "WORD_BELT";

    public WORD_Belt(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}