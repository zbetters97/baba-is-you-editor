package data;

import application.GamePanel;
import entity.Entity;
import entity.character.*;
import entity.object.*;
import entity.tile_interactive.IT_Belt;
import entity.tile_interactive.IT_Wall;
import entity.tile_interactive.IT_Water;
import entity.word.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class EntityGenerator {

    public final Map<String, Supplier<Entity>> characterFactory = new LinkedHashMap<>();
    public final Map<String, Supplier<Entity>> objectFactory = new LinkedHashMap<>();
    public final Map<String, Supplier<Entity>> wordNounFactory = new LinkedHashMap<>();
    public final Map<String, Supplier<Entity>> wordRuleFactory = new LinkedHashMap<>();
    public final Map<String, BiFunction<Integer, Integer, Entity>> iTileFactory = new LinkedHashMap<>();

    public EntityGenerator(GamePanel gp) {
        // Characters
        characterFactory.put(CHR_Baba.chrName, () -> new CHR_Baba(gp, 0, 0));
        characterFactory.put(CHR_Keke.chrName, () -> new CHR_Keke(gp, 0, 0));

        // Objects
        objectFactory.put(OBJ_Box.objName, () -> new OBJ_Box(gp, 0, 0));
        objectFactory.put(OBJ_Door.objName, () -> new OBJ_Door(gp, 0, 0));
        objectFactory.put(OBJ_Flag.objName, () -> new OBJ_Flag(gp, 0, 0));
        objectFactory.put(OBJ_Key.objName, () -> new OBJ_Key(gp, 0, 0));
        objectFactory.put(OBJ_Rock.objName, () -> new OBJ_Rock(gp, 0, 0));
        objectFactory.put(OBJ_Skull.objName, () -> new OBJ_Skull(gp, 0, 0));

        // Word nouns
        wordNounFactory.put(WORD_Baba.wordName, () -> new WORD_Baba(gp, 0, 0));
        wordNounFactory.put(WORD_Belt.wordName, () -> new WORD_Belt(gp, 0, 0));
        wordNounFactory.put(WORD_Box.wordName, () -> new WORD_Box(gp, 0, 0));
        wordNounFactory.put(WORD_Door.wordName, () -> new WORD_Door(gp, 0, 0));
        wordNounFactory.put(WORD_Flag.wordName, () -> new WORD_Flag(gp, 0, 0));
        wordNounFactory.put(WORD_Keke.wordName, () -> new WORD_Keke(gp, 0, 0));
        wordNounFactory.put(WORD_Key.wordName, () -> new WORD_Key(gp, 0, 0));
        wordNounFactory.put(WORD_Rock.wordName, () -> new WORD_Rock(gp, 0, 0));
        wordNounFactory.put(WORD_Skull.wordName, () -> new WORD_Skull(gp, 0, 0));
        wordNounFactory.put(WORD_Text.wordName, () -> new WORD_Text(gp, 0, 0));
        wordNounFactory.put(WORD_Wall.wordName, () -> new WORD_Wall(gp, 0, 0));
        wordNounFactory.put(WORD_Water.wordName, () -> new WORD_Water(gp, 0, 0));

        // Word rules
        wordRuleFactory.put(WORD_Is.wordName, () -> new WORD_Is(gp, 0, 0));
        wordRuleFactory.put(WORD_And.wordName, () -> new WORD_And(gp, 0, 0));
        wordRuleFactory.put(WORD_Has.wordName, () -> new WORD_Has(gp, 0, 0));
        wordRuleFactory.put(WORD_Defeat.wordName, () -> new WORD_Defeat(gp, 0, 0));
        wordRuleFactory.put(WORD_Float.wordName, () -> new WORD_Float(gp, 0, 0));
        wordRuleFactory.put(WORD_Hot.wordName, () -> new WORD_Hot(gp, 0, 0));
        wordRuleFactory.put(WORD_Melt.wordName, () -> new WORD_Melt(gp, 0, 0));
        wordRuleFactory.put(WORD_Open.wordName, () -> new WORD_Open(gp, 0, 0));
        wordRuleFactory.put(WORD_Push.wordName, () -> new WORD_Push(gp, 0, 0));
        wordRuleFactory.put(WORD_Shift.wordName, () -> new WORD_Shift(gp, 0, 0));
        wordRuleFactory.put(WORD_Shut.wordName, () -> new WORD_Shut(gp, 0, 0));
        wordRuleFactory.put(WORD_Sink.wordName, () -> new WORD_Sink(gp, 0, 0));
        wordRuleFactory.put(WORD_Stop.wordName, () -> new WORD_Stop(gp, 0, 0));
        wordRuleFactory.put(WORD_Weak.wordName, () -> new WORD_Weak(gp, 0, 0));
        wordRuleFactory.put(WORD_Win.wordName, () -> new WORD_Win(gp, 0, 0));
        wordRuleFactory.put(WORD_You.wordName, () -> new WORD_You(gp, 0, 0));

        // I_Tiles
        iTileFactory.put(IT_Belt.iName, (ori, _) -> new IT_Belt(gp, 0, 0, ori));
        iTileFactory.put(IT_Wall.iName, (ori, side) -> new IT_Wall(gp, 0, 0, ori, side));
        iTileFactory.put(IT_Water.iName, (ori, side) -> new IT_Water(gp, 0, 0, ori, side));
    }

    public Entity getEntity(String eName, int ori, int side) {
        Entity entity = getFromFactory(characterFactory, eName);
        if (entity != null) return entity;

        entity = getFromFactory(objectFactory, eName);
        if (entity != null) return entity;

        entity = getFromFactory(wordNounFactory, eName);
        if (entity != null) return entity;

        entity = getFromFactory(wordRuleFactory, eName);
        if (entity != null) return entity;

        return getITile(eName, ori, side);
    }

    private Entity getFromFactory(Map<String, Supplier<Entity>> factory, String name) {
        Supplier<Entity> supplier = factory.get(name);
        return supplier == null ? null : supplier.get();
    }

    private Entity getITile(String eName, int ori, int side) {
        BiFunction<Integer, Integer, Entity> factory = iTileFactory.get(eName);
        return factory != null ? factory.apply(ori, side) : null;
    }
}