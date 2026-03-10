package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Swap extends WordEntity {

    public static final String wordName = "WORD_SWAP";

    public WORD_Swap(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}