package application;

import entity.Entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static entity.Entity.Property.STOP;

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
     * @param targets List of entities to check collision against
     * @return Entity the given entity will interact with, -1 if none
     */
    public Entity checkEntity(Entity entity, ArrayList<Entity> targets) {

        Entity target = null;
        for (Entity t : targets) {
            entity.getHitbox().x = entity.getWorldX();
            entity.getHitbox().y = entity.getWorldY();

            t.getHitbox().x = t.getWorldX();
            t.getHitbox().y = t.getWorldY();

            // Entity and target collides
            if (entity.getHitbox().intersects(t.getHitbox()) && entity.getId() != t.getId()) {

                target = t;
                if (t.has(STOP) || t.getCollisionOn()) {
                    entity.setCollision(true);
                }
            }

            if (isOutOfBounds(entity.getHitbox().x, entity.getHitbox().y)) {
                entity.setCollision(true);
            }

            // Reset entity solid area
            entity.getHitbox().x = 0;
            entity.getHitbox().y = 0;

            // Reset target solid area
            t.getHitbox().x = 0;
            t.getHitbox().y = 0;
        }

        return target;
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