package application;

import entity.Entity;
import entity.Entity.Property;
import entity.WordEntity;
import entity.word.*;

import java.util.*;

public class LogicHandler {

    // Words mapped to a property
    private static final Map<String, Entity.Property> PROPERTY_MAP =
            Map.ofEntries(
                    Map.entry(WORD_Defeat.wordName, Property.DEFEAT),
                    Map.entry(WORD_Float.wordName, Property.FLOAT),
                    Map.entry(WORD_Hot.wordName, Property.HOT),
                    Map.entry(WORD_Melt.wordName, Property.MELT),
                    Map.entry(WORD_Open.wordName, Property.OPEN),
                    Map.entry(WORD_Push.wordName, Property.PUSH),
                    Map.entry(WORD_Shift.wordName, Property.SHIFT),
                    Map.entry(WORD_Shut.wordName, Property.SHUT),
                    Map.entry(WORD_Sink.wordName, Property.SINK),
                    Map.entry(WORD_Stop.wordName, Property.STOP),
                    Map.entry(WORD_Swap.wordName, Property.SWAP),
                    Map.entry(WORD_You.wordName, Property.YOU),
                    Map.entry(WORD_Weak.wordName, Property.WEAK),
                    Map.entry(WORD_Win.wordName, Property.WIN)
            );

    private static final ArrayList<String> linkingVerbs = new ArrayList<>(
            Arrays.asList(
                    WORD_Is.wordName,
                    WORD_Has.wordName
            )
    );

    private final GamePanel gp;
    public boolean rulesInitialized = false;
    private Set<String> newRules;
    private Set<String> activeRules = new HashSet<>();
    private final Map<String, List<String>> transformations = new HashMap<>();

    public LogicHandler(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * UPDATE
     * Runs the methods in LogicHandler
     * Called by GamePanel
     */
    public void scanForRules() {
        newRules = new HashSet<>();

        clearProperties();
        scanColumnRules();
        scanRowRules();
        applyTransformationRules();

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
    public void clearProperties() {
        for (Entity e : gp.entities) {
            e.clearProperties();
        }

        transformations.clear();
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
            for (Entity w : gp.entities) {
                if (!(w instanceof WordEntity)) continue;

                int x = w.getPoint().x / gp.tileSize;
                int y = w.getPoint().y / gp.tileSize;

                // Word's X matches column, add to corresponding Y (row) in list
                if (x == col) {
                    colWords[y] = w.getName();
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
            for (Entity w : gp.entities) {
                if (!(w instanceof WordEntity)) continue;

                int x = w.getPoint().x / gp.tileSize;
                int y = w.getPoint().y / gp.tileSize;

                // Word is on same row, add to corresponding X (column) in list
                if (y == row) {
                    rowWords[x] = w.getName();
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

        String verb;
        int i = 0;
        while (i < words.length - 2) {

            // Collect all subjects before connecting words (IS, HAS, AND...)
            List<String> subjects = new ArrayList<>();
            int k = i;
            while (k < words.length) {

                String subject = words[k];

                // Subject is an actual subject
                if (!subject.isEmpty() && !linkingVerbs.contains(subject) && !subject.equals(WORD_On.wordName)) {
                    subjects.add(subject.replace("WORD_", ""));
                }

                if (k + 1 >= words.length) break;

                // Break if next word is a linking verb or ON
                verb = words[k + 1];
                if (linkingVerbs.contains(verb) || verb.equals(WORD_On.wordName)) break;

                // Break if next word is not AND
                if (!words[k + 1].equals(WORD_And.wordName)) break;

                // AND continues the rule, continue to connecting set
                k += 2;
            }

            if (k + 1 >= words.length) {
                i = k + 1;
                continue;
            }

            // If rule contains ON
            String onTarget = null;
            if (words[k + 1].equals(WORD_On.wordName)) {
                if (k + 2 < words.length) {

                    // Capture entity the subject must be ON for rule to apply
                    onTarget = words[k + 2].replace("WORD_", "");

                    // Move past ON and its target
                    k += 2;
                }
            }

            // Break if linking verb is not after the rule
            if (k + 1 >= words.length || !linkingVerbs.contains(words[k + 1])) {
                i = k + 1;
                continue;
            }

            verb = words[k + 1];

            // Collect all predicates after connecting words
            int j = k + 2;
            while (j < words.length) {

                String predicate = words[j];

                // Potential new rule to apply
                Entity.Property property = PROPERTY_MAP.get(predicate);

                // Potential new form to apply
                String newFormName = predicate.replace("WORD_", "");
                Entity newForm = gp.eGenerator.getEntity(newFormName, 0, 0);

                // Rule found
                if (property != null) {
                    addProperties(subjects, onTarget, verb, predicate, property);
                }
                else if (newForm != null) {
                    addTransformations(subjects, onTarget, verb, predicate, newFormName);
                }

                // Break if next word is not AND
                if (j + 1 >= words.length || !words[j + 1].equals(WORD_And.wordName)) break;

                j += 2;
            }

            // Move past this rule chain
            i = j;
        }
    }

    private void addProperties(List<String> subjects, String onTarget, String verb, String predicate, Property property) {

        // Apply rule for all subjects
        for (String subject : subjects) {

            // Rule includes ON condition
            if (onTarget != null) {

                // Find subject
                for (Entity e : gp.entities) {
                    if (!e.getName().equals(subject)) continue;

                    // Find target
                    for (Entity target : gp.entities) {
                        if (!target.getName().equals(onTarget)) continue;

                        // Subject and target are on top of each other
                        if (target.getPoint().equals(e.getPoint())) {
                            String ruleString = subject + " ON " + onTarget + " " + verb + " " + predicate;
                            newRules.add(ruleString);
                            applyPropertyRule(subject, property);
                        }
                    }
                }
            }
            else {
                String ruleString = subject + " " + verb + " " + predicate;
                newRules.add(ruleString);
                applyPropertyRule(subject, property);
            }
        }
    }
    private void applyPropertyRule(String entityName, Entity.Property property) {
        for (Entity e : gp.entities) {

            // If entity's name matches passed name, provide property
            if (e.getName().equals(entityName) || entityName.equals("TEXT") && e instanceof WordEntity) {
                e.addProperty(property);
            }
        }
    }

    private void addTransformations(List<String> subjects, String onTarget, String verb, String predicate, String newFormName) {

        // Apply rule for all subjects
        for (String subject : subjects) {

            // Rule includes ON condition
            if (onTarget != null) {

                // Find subject
                for (Entity e : gp.entities) {
                    if (!e.getName().equals(subject)) continue;

                    // Find target
                    for (Entity target : gp.entities) {
                        if (!target.getName().equals(onTarget)) continue;

                        // If subject and target are on top of each other
                        if (target.getPoint().equals(e.getPoint())) {
                            String ruleString = subject + " ON " + onTarget + " " + verb + " " + predicate;
                            newRules.add(ruleString);

                            // Rule gives held entity to subject
                            if (verb.equals(WORD_Has.wordName)) {
                                applyHasRule(subject, newFormName);
                            }
                            else if (verb.equals(WORD_Is.wordName)) {
                                transformations.computeIfAbsent(subject, _ -> new ArrayList<>()).add(newFormName);
                            }
                        }
                    }
                }
            }
            else {
                String ruleString = subject + " " + verb + " " + predicate;
                newRules.add(ruleString);

                // Rule gives held entity to subject
                if (verb.equals(WORD_Has.wordName)) {
                    applyHasRule(subject, newFormName);
                }
                else if (verb.equals(WORD_Is.wordName)) {
                    transformations.computeIfAbsent(subject, _ -> new ArrayList<>()).add(newFormName);
                }
            }
        }
    }
    private void applyHasRule(String entityName, String newFormName) {
        for (Entity e : gp.entities) {

            // If entity's name matches passed name, provide property
            if (e.getName().equals(entityName)) {
                Entity newForm = gp.eGenerator.getEntity(newFormName, 0, 0);
                e.giveHeldEntity(newForm);
            }
        }
    }

    private void applyTransformationRules() {

        // Parse over each transformation rule
        for (Map.Entry<String, List<String>> entry : transformations.entrySet()) {
            String subject = entry.getKey();

            // Parse over each new form
            for (String newFormName : entry.getValue()) {
                for (Entity e : gp.entities) {

                    // If transforming to self, lock transformation
                    if (e.getName().equals(subject) && e.getName().equals(newFormName)) {
                        e.setTransformationLock(true);
                    }
                }
            }
        }

        // Parse over each mapped transformation
        for (Map.Entry<String, List<String>> entry : transformations.entrySet()) {

            // Apply transformation rule for each assigned form to entity
            String subject = entry.getKey();
            for (String newFormName : entry.getValue()) {
                for (Entity e : gp.entities) {

                    // If entity's name matches passed name, transform to new entity
                    if (e.getName().equals(subject) && !e.getTransformationLock()) {
                        Entity newForm = gp.eGenerator.getEntity(newFormName, 0, 0);
                        e.transform(newForm);
                    }
                }
            }
        }

        transformations.clear();
    }
}
