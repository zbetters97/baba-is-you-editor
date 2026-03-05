package application;

import entity.Entity;
import entity.Entity.Property;
import entity.WordEntity;
import entity.word.*;

import java.util.*;

class LogicHandler {

    // Words mapped to a property
    private static final Map<String, Entity.Property> PROPERTY_MAP =
            Map.ofEntries(
                    Map.entry(WORD_Defeat.wordName, Property.DEFEAT),
                    Map.entry(WORD_Float.wordName, Property.FLOAT),
                    Map.entry(WORD_Hot.wordName, Property.HOT),
                    Map.entry(WORD_Melt.wordName, Property.MELT),
                    Map.entry(WORD_Open.wordName, Property.OPEN),
                    Map.entry(WORD_Push.wordName, Property.PUSH),
                    Map.entry(WORD_Shut.wordName, Property.SHUT),
                    Map.entry(WORD_Sink.wordName, Property.SINK),
                    Map.entry(WORD_Stop.wordName, Property.STOP),
                    Map.entry(WORD_You.wordName, Property.YOU),
                    Map.entry(WORD_Weak.wordName, Property.WEAK),
                    Map.entry(WORD_Win.wordName, Property.WIN)
            );

    private final GamePanel gp;
    private boolean rulesInitialized = false;
    private Set<String> activeRules = new HashSet<>();

    public LogicHandler(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * UPDATE
     * Runs the methods in LogicHandler
     * Called by GamePanel
     */
    public void scanForRules() {
        Set<String> newRules = new HashSet<>();

        clearProperties();
        scanColumnRules(newRules);
        scanRowRules(newRules);

        // Play sound if new rule was created
        if (rulesInitialized) {
            for (String rule : newRules) {
                if (!activeRules.contains(rule)) {
                    gp.playSE(3, 0);
                    break;
                }
            }
        }

        activeRules = newRules;
        rulesInitialized = true;
    }

    /**
     * CLEAR PROPERTIES
     * Resets all rules currently in place
     * WORDS never have properties
     * Called by update();
     */
    private void clearProperties() {
        for (Entity e : gp.entities) {
            if (e == null) continue;

            e.clearProperties();
            e.getHeldEntities().clear();
        }
    }

    /**
     * SCAN COLUMN RULES
     * Scans each column to find valid rules
     * Called by update()
     */
    private void scanColumnRules(Set<String> rules) {

        // Loop over each column (horizontally)
        for (int col = 0; col < gp.maxWorldCol; col++) {

            // Create new array per column, fill with blanks
            String[] colWords = new String[gp.maxWorldRow];
            Arrays.fill(colWords, "");

            // Loop over all pre-existing words
            for (Entity w : gp.entities) {
                if (!(w instanceof WordEntity)) continue;

                int x = w.getWorldX() / gp.tileSize;
                int y = w.getWorldY() / gp.tileSize;

                // Word's X matches column, add to corresponding Y (row) in list
                if (x == col) {
                    colWords[y] = w.getName();
                }
            }

            // Check if any rules are active
            checkRules(colWords, rules);
        }
    }

    /**
     * SCAN ROW RULES
     * Scans each row to find valid rules
     * Called by update()
     */
    private void scanRowRules(Set<String> rules) {

        // Loop over each row (vertically)
        for (int row = 0; row < gp.maxWorldRow; row++) {

            // Create new array per row, fill with blanks
            String[] rowWords = new String[gp.maxWorldCol];
            Arrays.fill(rowWords, "");

            // Loop over all pre-existing words
            for (Entity w : gp.entities) {
                if (!(w instanceof WordEntity)) continue;

                int x = w.getWorldX() / gp.tileSize;
                int y = w.getWorldY() / gp.tileSize;

                // Word is on same row, add to corresponding X (column) in list
                if (y == row) {
                    rowWords[x] = w.getName();
                }
            }

            // Check if any rules are active
            checkRules(rowWords, rules);
        }
    }

    /**
     * PARSE RULES
     * Parses through given array string and assigns properties where applicable
     * @param words Array of words to parse through
     */
    private void checkRules(String[] words, Set<String> rules) {

        String verb;
        int i = 0;
        while (i < words.length - 2) {

            // Collect all Subjects before IS
            List<String> subjects = new ArrayList<>();
            int k = i;
            while (k < words.length) {

                String subj = words[k].replace("WORD_", "");
                if (!subj.isEmpty()
                        && !words[k].equals(WORD_Is.wordName)
                        && !words[k].equals(WORD_And.wordName)
                        && !words[k].equals(WORD_Has.wordName)) {
                    subjects.add(subj);
                }

                if (k + 1 >= words.length) break;

                // Break if next word is IS
                verb = words[k + 1];
                if (k + 1 >= words.length || verb.equals(WORD_Is.wordName) || verb.equals(WORD_Has.wordName)) break;

                // Break if next word is not AND
                if (!words[k + 1].equals(WORD_And.wordName)) break;

                k += 2;
            }

            if (k + 1 >= words.length) {
                i = k + 1;
                continue;
            }

            // Continue if IS or HAS is after subject(s)
            verb = words[k + 1];
            if (k + 1 >= words.length || !verb.equals(WORD_Is.wordName) && !verb.equals(WORD_Has.wordName)) {
                i = k + 1;
                continue;
            }

            // Collect all predicates after IS
            int j = k + 2;
            while (j < words.length) {
                String predicate = words[j];

                // Potential new rule to apply
                Entity.Property property = PROPERTY_MAP.get(predicate);

                // Potential new form to apply
                Entity newForm = gp.eGenerator.getEntity(predicate.replace("WORD_", ""), 0, 0);

                // Rule found
                if (property != null || newForm != null) {

                    // Apply rule for all subjects
                    for (String subject : subjects) {

                        String ruleString = subject + " " + verb + " " + predicate;
                        rules.add(ruleString);

                        if (verb.equals(WORD_Has.wordName) && newForm != null) {
                            applyHasRule(subject, newForm);
                        }
                        else if (property != null) {
                            applyPropertyRule(subject, property);
                        }
                        else {
                            applyTransformationRule(subject, newForm);
                        }
                    }
                }

                // Break if next word is not AND
                if (j + 1 >= words.length || !words[j + 1].equals(WORD_And.wordName)) break;
                j += 2;
            }

            // Move past this rule chain
            i++;
        }
    }

    private void applyHasRule(String entityName, Entity form) {
        for (Entity e : gp.entities) {
            if (e == null) continue;

            // If entity's name matches passed name, provide property
            if (e.getName().equals(entityName)) {
                e.giveHeldEntity(form);
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
        for (Entity e : gp.entities) {
            if (e == null) continue;

            // If entity's name matches passed name, provide property
            if (e.getName().equals(entityName) || entityName.equals("TEXT") && e instanceof WordEntity) {
                e.addProperty(property);
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

        if (newForm.getName().equals(oldEntityName)) return;

        for (Entity e : gp.entities) {
            if (e == null) continue;

            // If entity's name matches passed name, transform to new entity
            if (e.getName().equals(oldEntityName)) {
                e.transform(newForm);
            }
        }
    }
}
