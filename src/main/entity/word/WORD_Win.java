package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Win extends WordEntity {

    public static final String wordName = "WORD_WIN";

    public WORD_Win(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}