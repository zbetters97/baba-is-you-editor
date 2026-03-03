package entity.character;

import application.GamePanel;
import entity.CharacterEntity;

public class CHR_Baba extends CharacterEntity {

    public static final String chrName = "BABA";

    public CHR_Baba(GamePanel gp, int col, int row) {
        super(gp, col, row, chrName);
    }
}