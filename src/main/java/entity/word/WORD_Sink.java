package entity.word;

import application.GamePanel;
import entity.WordEntity;

public class WORD_Sink extends WordEntity {

    public static final String wordName = "WORD_SINK";

    public WORD_Sink(GamePanel gp, int col, int row) {
        super(gp, col, row, wordName);
    }
}