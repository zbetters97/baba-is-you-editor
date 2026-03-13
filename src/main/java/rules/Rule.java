package rules;

import application.GamePanel;
import entity.Entity;
import entity.WordEntity;
import entity.word.*;

import java.awt.*;
import java.util.Objects;

public class Rule {
    private final GamePanel gp;
    private final String subject;
    private final String preposition;
    private final String target;
    private final Properties property;
    private final String transformation;

    public Rule(GamePanel gp, String subject, String preposition, String target, Properties property, String transformation) {
        this.gp = gp;
        this.subject = subject;
        this.preposition = preposition;
        this.target = target;
        this.property = property;
        this.transformation = transformation;
    }

    public void runRule(Entity self) {

        // Rule needs to meet a condition
        if (conditional()) {
            for (Entity e : gp.entities) {
                // Entity is self or not required target
                if (e == self || !target.equals(e.getName())) continue;

                // Apply if condition is met
                if (ruleApplies(self, e)) {
                    applyRule(self);
                    return;
                }
            }
        }
        else {
            applyRule(self);
        }
    }

    private boolean conditional() {
        return switch (preposition) {
            case WORD_On.wordName,
                 WORD_Near.wordName,
                 WORD_Next.wordName,
                 WORD_Facing.wordName,
                 WORD_Seeing.wordName -> true;
            default -> false;
        };
    }

    private void applyRule(Entity self) {
        if (property != null) {
            self.addProperty(property);
        }
        else if (transformation != null) {
            if (WORD_Has.wordName.equals(preposition)) {

                boolean alreadyHeld = self.getHeldEntities().stream()
                        .anyMatch(e -> e.getName().equals(transformation));

                if (!alreadyHeld) {
                    Entity newForm = gp.eGenerator.getEntity(transformation, 0, 0);
                    self.giveHeldEntity(newForm);
                }
            }
            else {
                if (self.getTransformationLock() || self.getName().equals(transformation)) return;

                Entity newForm = gp.eGenerator.getEntity(transformation, 0, 0);
                self.transform(newForm);
            }
        }
    }

    private boolean ruleApplies(Entity self, Entity t) {
        return switch (preposition) {
            case WORD_On.wordName -> on(self, t);
            case WORD_Near.wordName -> near(self, t);
            case WORD_Next.wordName -> next(self, t);
            case WORD_Facing.wordName -> facing(self, t);
            case WORD_Seeing.wordName -> seeing(self, t);
            default -> true;
        };
    }
    private boolean on(Entity self, Entity t) {
        return self.getPoint().equals(t.getPoint());
    }
    private boolean near(Entity self, Entity t) {
        int dx = Math.abs(t.getPoint().x - self.getPoint().x);
        int dy = Math.abs(t.getPoint().y - self.getPoint().y);

        return dx <= gp.tileSize && dy <= gp.tileSize && !(dx == 0 && dy == 0);
    }
    private boolean next(Entity self, Entity t) {
        int dx = Math.abs(t.getPoint().x - self.getPoint().x);
        int dy = Math.abs(t.getPoint().y - self.getPoint().y);

        return (dx == gp.tileSize && dy == 0) || (dy == gp.tileSize && dx == 0);
    }
    private boolean facing(Entity self, Entity t) {
        Point p = switch (self.getDirection()) {
            case UP -> new Point(self.getPoint().x, self.getPoint().y - gp.tileSize);
            case DOWN -> new Point(self.getPoint().x, self.getPoint().y + gp.tileSize);
            case LEFT -> new Point(self.getPoint().x - gp.tileSize, self.getPoint().y);
            case RIGHT -> new Point(self.getPoint().x + gp.tileSize, self.getPoint().y);
        };

        return t.getPoint().equals(p);
    }
    private boolean seeing(Entity self, Entity t) {

        int dx = 0;
        int dy = 0;

        switch (self.getDirection()) {
            case UP -> dy = -gp.tileSize;
            case DOWN -> dy = gp.tileSize;
            case LEFT -> dx =- gp.tileSize;
            case RIGHT -> dx = gp.tileSize;
        }

        int x = self.getPoint().x + dx;
        int y = self.getPoint().y + dy;

        while (!gp.cChecker.isOutOfBounds(x, y)) {
            for (Entity e : gp.entities) {
                if (e.getPoint().x == x && e.getPoint().y == y) {
                    if (e.has(Properties.STOP) || e.has(Properties.PUSH) || e instanceof WordEntity) {
                        return false;
                    }
                    if (e.getName().equals(t.getName())) {
                        return true;
                    }
                }
            }

            x += dx;
            y += dy;
        }

        return false;
    }

    public String getSubject() {
        return subject;
    }
    public String getPreposition() {
        return preposition;
    }
    public String getTransformation() {
        return transformation != null ? transformation : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rule r)) return false;
        return Objects.equals(subject, r.subject)
                && Objects.equals(preposition, r.preposition)
                && Objects.equals(target, r.target)
                && Objects.equals(property, r.property)
                && Objects.equals(transformation, r.transformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, preposition, target, property, transformation);
    }
}
