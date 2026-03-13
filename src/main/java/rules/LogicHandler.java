package rules;

import application.GamePanel;
import entity.Entity;
import entity.WordEntity;
import entity.word.*;

import java.util.*;

import static rules.Properties.*;

public class LogicHandler {

    // Words mapped to a property
    private static final Map<String, Properties> PROPERTY_MAP =
            Map.ofEntries(
                    Map.entry(WORD_Defeat.wordName, DEFEAT),
                    Map.entry(WORD_Float.wordName, FLOAT),
                    Map.entry(WORD_Hot.wordName, HOT),
                    Map.entry(WORD_Melt.wordName, MELT),
                    Map.entry(WORD_Open.wordName, OPEN),
                    Map.entry(WORD_Push.wordName, PUSH),
                    Map.entry(WORD_Shift.wordName, SHIFT),
                    Map.entry(WORD_Shut.wordName, SHUT),
                    Map.entry(WORD_Sink.wordName, SINK),
                    Map.entry(WORD_Stop.wordName, STOP),
                    Map.entry(WORD_Swap.wordName, SWAP),
                    Map.entry(WORD_You.wordName, YOU),
                    Map.entry(WORD_Weak.wordName, WEAK),
                    Map.entry(WORD_Win.wordName, WIN)
            );

    private static final ArrayList<String> linkingVerbs = new ArrayList<>(
            Arrays.asList(
                    WORD_Is.wordName,
                    WORD_Has.wordName
            )
    );

    private static final ArrayList<String> prepositions = new ArrayList<>(
            Arrays.asList(
                    WORD_On.wordName,
                    WORD_Near.wordName,
                    WORD_Next.wordName,
                    WORD_Facing.wordName,
                    WORD_Seeing.wordName
            )
    );

    private final GamePanel gp;
    public boolean rulesInitialized = false;

    private final Set<Rule> activeRules = new HashSet<>();
    private final Set<Rule> conditionalRules = new HashSet<>();

    public LogicHandler(GamePanel gp) {
        this.gp = gp;
    }

    public void clearRules() {
        activeRules.clear();
        conditionalRules.clear();
        rulesInitialized = false;
    }

    public void scanForRules() {

        Set<Rule> newRules = new HashSet<>();
        Set<Rule> newStaticRules = new HashSet<>();
        Set<Rule> newConditionalRules = new HashSet<>();

        // Scan all rows and columns
        scanColumnRules(newRules);
        scanRowRules(newRules);

        // Separate static vs conditional
        for (Rule r : newRules) {
            if (isConditional(r.getPreposition())) {
                newConditionalRules.add(r);
            }
            else {
                newStaticRules.add(r);
            }
        }

        boolean newRuleAppeared = rulesInitialized && newRules.stream().anyMatch(r -> !activeRules.contains(r));
        if (newRuleAppeared) {
            playRuleSound();
        }

        conditionalRules.clear();
        conditionalRules.addAll(newConditionalRules);

        applyStaticRules(newStaticRules);

        activeRules.clear();
        activeRules.addAll(newRules);

        rulesInitialized = true;
    }

    private void playRuleSound() {
        gp.playSE(3, 0);
    }

    private void applyStaticRules(Set<Rule> newStaticRules) {

        resetEntityRuleStates();

        for (Rule rule : newStaticRules) {
            if (rule.getTransformation() != null) {
                for (Entity e : gp.entities) {
                    if ((e.getName().equals(rule.getSubject()) || (rule.getSubject().equals("TEXT") && e instanceof WordEntity)) &&
                            e.getName().equals(rule.getTransformation())) {
                        e.setTransformationLock(true);
                    }
                }
            }
        }

        for (Rule rule : newStaticRules) {
            for (Entity e : gp.entities) {
                if (e.getName().equals(rule.getSubject()) || (rule.getSubject().equals("TEXT") && e instanceof WordEntity)) {
                    rule.runRule(e);
                }
            }
        }
    }

    public void applyConditionalRules() {

        resetEntityRuleStates();

        for (Rule rule : conditionalRules) {
            for (Entity e : gp.entities) {
                if (e.getName().equals(rule.getSubject()) || (rule.getSubject().equals("TEXT") && e instanceof WordEntity)) {
                    rule.runRule(e);
                }
            }
        }
    }

    private void resetEntityRuleStates() {
        for (Entity e : gp.entities) {
            e.clearProperties();
            e.setTransformationLock(false);
        }
    }

    /**
     * SCAN COLUMN RULES
     * Scans each column to find valid rules
     * Called by update()
     */
    private void scanColumnRules(Set<Rule> newRules) {

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
            checkRules(colWords, newRules);
        }
    }

    /**
     * SCAN ROW RULES
     * Scans each row to find valid rules
     * Called by update()
     */
    private void scanRowRules(Set<Rule> newRules) {

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
            checkRules(rowWords, newRules);
        }
    }

    /**
     * PARSE RULES
     * Parses through given array string and assigns properties where applicable
     * @param words Array of words to parse through
     */
    private void checkRules(String[] words, Set<Rule> newRules) {

        String verb;
        int i = 0;
        while (i < words.length - 2) {

            // Collect all subjects before connecting words (IS, HAS, AND...)
            List<String> subjects = new ArrayList<>();
            int k = i;
            while (k < words.length) {

                String subject = words[k];

                // Subject is an actual subject
                if (!subject.isEmpty() && !linkingVerbs.contains(subject) && !prepositions.contains(subject)) {
                    subjects.add(subject.replace("WORD_", ""));
                }

                if (k + 1 >= words.length) break;

                // Break if next word is a linking verb or a preposition
                verb = words[k + 1];
                if (linkingVerbs.contains(verb) || prepositions.contains(verb)) break;

                // Break if next word is not AND
                if (!verb.equals(WORD_And.wordName)) break;

                // AND continues the rule, continue to connecting set
                k += 2;
            }

            if (k + 1 >= words.length) {
                i = k + 1;
                continue;
            }

            // If rule contains subject + preposition + target (ex: BABA NEAR ROCK)
            String target = null;
            String preposition = words[k + 1];
            if (!words[k].isEmpty() && prepositions.contains(preposition)) {
                if (k + 2 >= words.length) break;

                // If target word is a Property, don't do anything
                if (PROPERTY_MAP.get(words[k + 2]) != null) {
                    i = k + 1;
                    continue;
                }

                // Capture entity the subject must be related to for rule to apply
                target = words[k + 2].replace("WORD_", "");

                // Move past preposition and its target
                k += 2;
            }

            // Break if linking verb is not after the rule
            if (k + 1 >= words.length || !linkingVerbs.contains(words[k + 1])) {
                i = k + 1;
                continue;
            }

            // Collect all predicates after connecting words
            int j = k + 2;
            while (j < words.length) {

                // Potential new property to apply
                String predicate = words[j];
                Properties property = PROPERTY_MAP.get(predicate);

                // Potential new form to apply
                String newFormName = predicate.replace("WORD_", "");
                Entity newForm = gp.eGenerator.getEntity(newFormName, 0, 0);

                for (String subject : subjects) {

                    Rule rule;
                    if (property != null) {
                        rule = new Rule(gp, subject, preposition, target, property, null);
                    }
                    else if (newForm != null) {
                        rule = new Rule(gp, subject, preposition, target, null, newFormName);
                    }
                    else {
                        continue;
                    }
                    newRules.add(rule);
                }

                // Break if next word is not AND
                if (j + 1 >= words.length || !words[j + 1].equals(WORD_And.wordName)) {
                    break;
                }

                j += 2;
            }

            // Move past this rule chain
            i = j;
        }
    }

    private boolean isConditional(String preposition) {
        return switch (preposition) {
            case WORD_On.wordName, WORD_Near.wordName, WORD_Next.wordName, WORD_Facing.wordName, WORD_Seeing.wordName -> true;
            default -> false;
        };
    }
}
