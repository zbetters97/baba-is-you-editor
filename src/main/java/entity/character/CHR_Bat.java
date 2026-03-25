package entity.character;

import application.GamePanel;
import entity.CharacterEntity;

public class CHR_Bat extends CharacterEntity {

    public static final String chrName = "BAT";

    public CHR_Bat(GamePanel gp, int col, int row) {
        super(gp, col, row, chrName);
    }
}