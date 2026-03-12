package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Facing extends WordEntity {

    public static final String wordName = "WORD_FACING";

    public WORD_Facing(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}