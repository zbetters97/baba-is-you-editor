package application;

import entity.Entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public record CollisionChecker(GamePanel gp) {

    /**
     * GET ENTITIES AT NEXT TILE
     * Get list of entities that are at the tile to be moved on
     * @param entity Entity that is moving to the tile
     * @param dir Direction the entity is moving
     * @return List of entities found at tile
     */
    public List<Entity> getEntitiesAtNextTile(Entity entity, GamePanel.Direction dir) {

        List<Entity> result = new ArrayList<>();

        Point next = getNextTilePosition(entity, dir);
        for (Entity e : gp.entities) {
            if (e.getWorldX() == next.x && e.getWorldY() == next.y) {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * GET NEXT TILE POSITION
     * Gets the X/Y the entity is moving towards
     * @param entity The entity that is moving
     * @param dir The direction the entity is moving
     * @return The X/Y Point the entity will end on
     */
    private Point getNextTilePosition(Entity entity, GamePanel.Direction dir) {

        int nextX = 0, nextY = 0;

        switch (dir) {
            case UP -> nextY = -gp.tileSize;
            case DOWN -> nextY = gp.tileSize;
            case LEFT -> nextX = -gp.tileSize;
            case RIGHT -> nextX = gp.tileSize;
        }

        return new Point(entity.getWorldX() + nextX, entity.getWorldY() + nextY);
    }

    /**
     * CHECK ENTITY
     * Detects if given entity will collide with any entity from the given list
     * @param entity Entity to check collision on
     * @return List of entities the given entity will interact with
     */
    public ArrayList<Entity> checkEntity(Entity entity) {

        ArrayList<Entity> targets = new ArrayList<>();

        for (Entity t : gp.entities) {
            if (t == entity) continue;

            if (t.getWorldX() == entity.getWorldX() && t.getWorldY() == entity.getWorldY()) {
                targets.add(t);
            }
        }

        return targets;
    }

    /**
     * IS OUT-OF-BOUNDS
     * Checks if the given entity's hitbox is out of world boundary
     * @param entity Entity to check out of bounds on
     * @param dir Direction Entity is pointing towards
     * @return True if entity is out of bounds
     */
    public boolean isOutOfBounds(Entity entity, GamePanel.Direction dir) {

        Point next = getNextTilePosition(entity, dir);

        return next.x < 0 || next.x > gp.screenWidth - gp.tileSize ||
                next.y < 0 || next.y > gp.screenHeight - gp.tileSize;
    }
}