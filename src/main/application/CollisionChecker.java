package application;

import entity.Entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public record CollisionChecker(GamePanel gp) {

    /**
     * GET ENTITIES AT NEXT TILE
     * @param entity Entity that is moving to the tile
     * @param dir Direction the entity is moving
     * @return List of entities found at tile
     */
    public List<Entity> getEntitiesAtNextTile(Entity entity, GamePanel.Direction dir) {
        Point next = getNextTilePosition(entity, dir);

        // Get all entities at the next tile
        List<Entity> result = new ArrayList<>();
        for (Entity[] entities : gp.getAllEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;

                if (e.worldX == next.x && e.worldY == next.y) {
                    result.add(e);
                }
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

        return new Point(entity.worldX + nextX, entity.worldY + nextY);
    }


    /**
     * CHECK ENTITY
     * Detects if given entity will collide with any entity from the given list
     * @param entity Entity to check collision on
     * @param targets List of entities to check collision against
     * @return Entity the given entity will interact with, -1 if none
     */
    public int checkEntity(Entity entity, Entity[] targets) {

        int index = -1;
        for (int i = 0; i < targets.length; i++) {
            if (targets[i] == null) continue;

            entity.hitbox.x = entity.worldX;
            entity.hitbox.y = entity.worldY;

            targets[i].hitbox.x = targets[i].worldX;
            targets[i].hitbox.y = targets[i].worldY;

            // Entity and target collides
            if (entity.hitbox.intersects(targets[i].hitbox)) {
                if (targets[i] == entity) continue;

                index = i;
                if (targets[i].properties.contains(Entity.Property.STOP) || targets[i].collisionOn) {
                    entity.collisionOn = true;
                }
            }

            if (isOutOfBounds(entity.hitbox.x, entity.hitbox.y)) {
                entity.collisionOn = true;
            }

            // Reset entity solid area
            entity.hitbox.x = 0;
            entity.hitbox.y = 0;

            // Reset target solid area
            targets[i].hitbox.x = 0;
            targets[i].hitbox.y = 0;
        }

        return index;
    }

    /**
     * IS OUT-OF-BOUNDS
     * Checks if the given X/Y is out of world boundary
     * @param x X coordinate of entity
     * @param y Y coordinate of entity
     * @return True if entity is out of bounds
     */
    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x > gp.screenWidth - gp.tileSize ||
                y < 0 || y > gp.screenHeight - gp.tileSize;
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