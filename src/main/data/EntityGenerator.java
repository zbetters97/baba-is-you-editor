package data;

import application.GamePanel;
import entity.Entity;
import entity.character.*;
import entity.object.*;
import entity.tile_interactive.IT_Wall;
import entity.tile_interactive.IT_Water;
import entity.word.*;

public record EntityGenerator(GamePanel gp) {

    public Entity getEntity(String eName) {
        return getCharacter(eName) != null
                ? getCharacter(eName)
                : getObject(eName) != null
                    ? getObject(eName)
                    : getITile(eName) != null
                        ? getITile(eName)
                        : getWord(eName);
    }

    public Entity getCharacter(String cName) {
        return cName.equals(CHR_Baba.chrName) ?
            new CHR_Baba(gp, 0, 0)
            : null;
    }

    public Entity getObject(String eName) {
        return switch (eName) {
            case OBJ_Flag.objName -> new OBJ_Flag(gp, 0, 0);
            case OBJ_Rock.objName -> new OBJ_Rock(gp, 0,0);
            case OBJ_Skull.objName -> new OBJ_Skull(gp, 0,0);
            default -> null;
        };
    }

    public Entity getITile(String eName) {
        return switch (eName) {
            case IT_Wall.iName -> new IT_Wall(gp, 0, 0, 0, 0);
            case IT_Water.iName -> new IT_Water(gp, 0,0);
            default -> null;
        };
    }

    public Entity getWord(String wName) {
        return switch(wName) {
            case WORD_Baba.wordName -> new WORD_Baba(gp, 0, 0);
            case WORD_Defeat.wordName -> new WORD_Defeat(gp, 0,0);
            case WORD_Flag.wordName -> new WORD_Flag(gp, 0,0);
            case WORD_Is.wordName -> new WORD_Is(gp, 0,0);
            case WORD_Push.wordName -> new WORD_Push(gp, 0,0);
            case WORD_Rock.wordName -> new WORD_Rock(gp, 0,0);
            case WORD_Skull.wordName -> new WORD_Skull(gp, 0,0);
            case WORD_Sink.wordName -> new WORD_Sink(gp, 0,0);
            case WORD_Stop.wordName -> new WORD_Stop(gp, 0,0);
            case WORD_Wall.wordName -> new WORD_Wall(gp, 0,0);
            case WORD_Water.wordName -> new WORD_Water(gp, 0,0);
            case WORD_Win.wordName -> new WORD_Win(gp, 0,0);
            case WORD_You.wordName -> new WORD_You(gp, 0,0);
            default -> null;
        };
    }
}