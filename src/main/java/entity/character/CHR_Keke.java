package entity.character;

import application.GamePanel;
import entity.CharacterEntity;

public class CHR_Keke extends CharacterEntity {

    public static final String chrName = "KEKE";

    public CHR_Keke(GamePanel gp, int col, int row) {
        super(gp, col, row, chrName);
    }
}