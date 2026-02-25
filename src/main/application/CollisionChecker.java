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
     * TILE BLOCKED
     * @param entity The entity that is moving a tile
     * @param dir The direction the entity is moving
     * @return True if tile with collision is on next tile
     */
    public boolean tileBlocked(Entity entity, GamePanel.Direction dir) {

        Point next = getNextTilePosition(entity, dir);

        // Tile out of bounds
        if (gp.cChecker.isOutOfBounds(next.x, next.y)) {
            return true;
        }

        int col = next.x / gp.tileSize;
        int row = next.y / gp.tileSize;

        // Retrieve tile object at index
        int tile = gp.tileM.lvlTileNum[gp.currentLvl][col][row];

        // True if tile has collision
        return gp.tileM.tiles[tile].hasCollision;
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
    public int checkEntity(Entity entity, Entity[][] targets) {

        int index = -1;
        for (int i = 0; i < targets[0].length; i++) {
            if (targets[gp.currentLvl][i] == null) continue;

            entity.hitbox.x = entity.worldX;
            entity.hitbox.y = entity.worldY;

            targets[gp.currentLvl][i].hitbox.x = targets[gp.currentLvl][i].worldX;
            targets[gp.currentLvl][i].hitbox.y = targets[gp.currentLvl][i].worldY;

            // Entity and target collides
            if (entity.hitbox.intersects(targets[gp.currentLvl][i].hitbox)) {
                if (targets[gp.currentLvl][i] == entity) continue;

                index = i;
                if (targets[gp.currentLvl][i].properties.contains(Entity.Property.STOP) || targets[gp.currentLvl][i].collisionOn) {
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
            targets[gp.currentLvl][i].hitbox.x = 0;
            targets[gp.currentLvl][i].hitbox.y = 0;
        }

        return index;
    }

    /**
     * IS OUT-OF-BOUNDS
     * Checks if the given entity's hitbox is out of world boundary
     * @param x X coordinate of entity
     * @param y Y coordinate of entity
     * @return True if entity is out of bounds
     */
    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x > gp.screenWidth - gp.tileSize ||
                y < 0 || y > gp.screenHeight - gp.tileSize;
    }
}