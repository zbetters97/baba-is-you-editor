package rules;

import application.GamePanel;
import entity.Entity;
import entity.WordEntity;

import java.awt.*;

public enum Properties {
    DEFEAT {
        @Override
        public void onTouch(Entity self, Entity other) {
            if (other.has(YOU) && self.isSameFloat(other)) {
                other.playSE(4, 3);
                other.kill();
            }
        }
    },
    FLOAT,
    HOT {
        @Override
        public void onTouch(Entity self, Entity other) {
            if (other.has(MELT) && self.isSameFloat(other)) {
                other.kill();
            }
        }
    },
    MELT,
    OPEN {
        @Override
        public void onTouch(Entity self, Entity other) {
            if (other.has(SHUT)) {
                self.playSE(4, 2);
                self.kill();
                other.kill();
            }
        }
    },
    PUSH {
        @Override
        public boolean allowsPush(Entity self) {
            return !self.has(SWAP);
        }
    },
    SHIFT {
        @Override
        public void onTouch(Entity self, Entity other) {
            if (self.isSameFloat(other) && !self.has(PUSH)) {
                other.move(self.getDirection());
            }
        }
    },
    SHUT {
        @Override
        public boolean blocksMovement(Entity self, Entity mover, GamePanel.Direction dir) {
            return !mover.has(OPEN);
        }
    },
    SINK {
        @Override
        public void onTouch(Entity self, Entity other) {
            // Both must be floating or not floating
            if (self.isSameFloat(other)) {
                other.playSE(4, 1);
                self.kill();
                other.kill();
            }
        }
    },
    STOP {
        @Override
        public boolean blocksMovement(Entity self, Entity mover, GamePanel.Direction dir) {
            return !self.has(SHUT) || !mover.has(OPEN);
        }
    },
    SWAP {
        @Override
        public boolean blocksMovement(Entity self, Entity mover, GamePanel.Direction dir) {
            Point otherPoint = mover.getPoint();

            mover.setPreviousPoint(self.getPoint());
            mover.setReversing(true);

            self.setPreviousPoint(otherPoint);
            self.setReversing(true);

            return false;
        }
    },
    WEAK {
        @Override
        public void onTouch(Entity self, Entity other) {
            // Both must be floating or not floating
            if (!(other instanceof WordEntity) && self.isSameFloat(other)) {
                self.playSE(4, 0);
                self.kill();
            }
        }
    },
    WIN {
        @Override
        public void onTouch(Entity self, Entity other) {
            if (other.has(YOU) && self.isSameFloat(other)) {
                self.playSE(1, 1);
                self.win();
            }
        }
    },
    YOU;

    public void onTouch(Entity self, Entity other) {}
    public boolean blocksMovement(Entity self, Entity mover, GamePanel.Direction dir) {
        return false;
    }
    public boolean allowsPush(Entity self) {
        return false;
    }
}
