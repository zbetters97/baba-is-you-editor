package application;

import entity.Entity;
import entity.word.*;

import java.util.Arrays;
import java.util.Map;

public record LogicHandler(GamePanel gp) {

    // Words mapped to a property
    private static final Map<String, Entity.Property> PROPERTY_MAP = Map.of(
            WORD_Win.wordName, Entity.Property.WIN,
            WORD_Stop.wordName, Entity.Property.STOP,
            WORD_Push.wordName, Entity.Property.PUSH,
            WORD_You.wordName, Entity.Property.YOU,
            WORD_Sink.wordName, Entity.Property.SINK,
            WORD_Defeat.wordName, Entity.Property.DEFEAT
    );

    /**
     * UPDATE
     * Runs the methods in LogicHandler
     * Called by GamePanel
     */
    public void scanForRules() {
        clearProperties();
        scanColumnRules();
        scanRowRules();
    }

    /**
     * CLEAR PROPERTIES
     * Resets all rules currently in place
     * WORDS never have properties
     * Called by update();
     */
    private void clearProperties() {
        for (Entity[] entities : gp.getAllRegularEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;

                e.properties.clear();
            }
        }
    }

    /**
     * SCAN COLUMN RULES
     * Scans each column to find valid rules
     * Called by update()
     */
    private void scanColumnRules() {

        // Loop over each column (horizontally)
        for (int col = 0; col < gp.maxWorldCol; col++) {

            // Create new array per column, fill with blanks
            String[] colWords = new String[gp.maxWorldRow];
            Arrays.fill(colWords, "");

            // Loop over all pre-existing words
            for (Entity word : gp.words[gp.currentLvl]) {
                if (word != null) {
                    int x = word.worldX / gp.tileSize;
                    int y = word.worldY / gp.tileSize;

                    // Word's X matches column, add to corresponding Y (row) in list
                    if (x == col) {
                        colWords[y] = word.name;
                    }
                }
            }

            // Check if any rules are active
            checkRules(colWords);
        }
    }

    /**
     * SCAN ROW RULES
     * Scans each row to find valid rules
     * Called by update()
     */
    private void scanRowRules() {

        // Loop over each row (vertically)
        for (int row = 0; row < gp.maxWorldRow; row++) {

            // Create new array per row, fill with blanks
            String[] rowWords = new String[gp.maxWorldCol];
            Arrays.fill(rowWords, "");

            // Loop over all pre-existing words
            for (Entity word : gp.words[gp.currentLvl]) {
                if (word != null) {
                    int x = word.worldX / gp.tileSize;
                    int y = word.worldY / gp.tileSize;

                    // Word is on same row, add to corresponding X (column) in list
                    if (y == row) {
                        rowWords[x] = word.name;
                    }
                }
            }

            // Check if any rules are active
            checkRules(rowWords);
        }
    }

    /**
     * PARSE RULES
     * Parses through given array string and assigns properties where applicable
     * @param words Array of words to parse through
     */
    private void checkRules(String[] words) {

        // Stop at (length - 2) to count 3 consecutive words (0, 1, 2)
        for (int i = 0; i < words.length - 2; i++) {

            // Object receiving the property (ex: FLAG)
            String subject = words[i].replace("WORD_", "");

            // Linking verb (ex: IS)
            String verb = words[i + 1];

            // The action applied to the object (ex: WIN)
            String predicate = words[i + 2];

            // Does not equal a rule
            if (subject.isEmpty() || predicate.isEmpty() || !verb.equals(WORD_Is.wordName)) {
                continue;
            }

            // Matching property to the predicate
            Entity.Property property = PROPERTY_MAP.get(predicate);
            if (property != null) {
                applyPropertyRule(subject, property);
                continue;
            }

            Entity newForm = gp.eGenerator.getEntity(predicate.replace("WORD_", ""));
            if (newForm != null) {
                applyTransformationRule(subject, newForm);
            }
        }
    }

    /**
     * APPLY RULE
     * Finds the objects that have the name
     * And assigns the given property to that object
     * Called by checkRules()
     * @param entityName The name of the entity to pass the property to
     * @param property The property the object will be receiving
     */
    private void applyPropertyRule(String entityName, Entity.Property property) {

        for (Entity[] entities : gp.getAllRegularEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;

                // If entity's name (current or base form) matches passed name, provide property
                if (e.name.equals(entityName)) {
                    e.properties.add(property);
                }
            }
        }
    }

    /**
     * APPLY TRANSFORMATION RULE
     * Runs transformation rules for all entities where applicable
     * Called by checkRules()
     * @param oldEntityName The name of the entity to be transformed
     * @param newForm The new entity to form into
     */
    private void applyTransformationRule(String oldEntityName, Entity newForm) {
        for (Entity[] entities : gp.getAllRegularEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;

                // If entity's name matches passed name, transform to new entity
                if (e.name.equals(oldEntityName)) {
                    e.setForm(newForm);
                }
            }
        }
    }
}
