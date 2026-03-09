package entity;

import application.GamePanel;
import application.GamePanel.Direction;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static application.GamePanel.Direction.*;

public class Entity {

    // Properties an Entity can have
    public enum Property {
        DEFEAT {
            @Override
            void onTouch(Entity self, Entity other) {
                if (other.has(YOU) && self.isSameFloat(other)) {
                    other.kill();
                }
            }
        },
        FLOAT,
        HOT {
            @Override
            void onTouch(Entity self, Entity other) {
                if (other.has(MELT) && self.isSameFloat(other)) {
                    other.kill();
                }
            }
        },
        MELT,
        OPEN {
            @Override
            void onTouch(Entity self, Entity other) {
                if (other.has(SHUT)) {
                    self.playSE(4, 2);
                    self.kill();
                    other.kill();
                }
            }
        },
        PUSH {
            @Override
            boolean allowsPush() {
                return true;
            }
        },
        SHIFT {
            @Override
            void onTouch(Entity self, Entity other) {
                if (self.isSameFloat(other) && !self.has(PUSH)) {
                    other.move(self.getDirection());
                }
            }
        },
        SHUT {
            @Override
            boolean blocksMovement(Entity self, Entity mover, Direction dir) {
                return !mover.has(OPEN);
            }
        },
        SINK {
            @Override
            void onTouch(Entity self, Entity other) {
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
            boolean blocksMovement(Entity self, Entity mover, Direction dir) {
                return true;
            }
        },
        WEAK {
            @Override
            void onTouch(Entity self, Entity other) {
                // Both must be floating or not floating
                if (!(other instanceof WordEntity) && self.isSameFloat(other)) {
                    self.playSE(4, 0);
                    self.kill();
                }
            }
        },
        WIN {
            @Override
            void onTouch(Entity self, Entity other) {
                if (other.has(YOU) && self.isSameFloat(other)) {
                    self.playSE(1, 1);
                    self.gp.win = true;
                }
            }
        },
        YOU;

        void onTouch(Entity self, Entity other) {}
        boolean blocksMovement(Entity self, Entity mover, Direction dir) {
            return false;
        }
        boolean allowsPush() {
            return false;
        }
    }

    // Empty enum list to hold properties
    private final EnumSet<Entity.Property> properties = EnumSet.noneOf(Entity.Property.class);

    protected GamePanel gp;

    private static int NEXT_ID = 0;
    private final int id;

    /* GENERAL ATTRIBUTES */
    protected int worldX, worldY;
    private int previousWorldX, previousWorldY;
    protected String name;
    protected int ori, side;
    private boolean alive = true;

    /* MOVEMENT VALUES */
    protected GamePanel.Direction direction = DOWN;
    private final int speed = 6;
    private boolean moving = false;
    private boolean reversing = false;
    private final ArrayList<Entity> heldEntities = new ArrayList<>();
    private boolean lockTransformation = false;

    /* COLLISION VALUES */
    private final Rectangle hitbox = new Rectangle(0, 0, 48, 48);
    private boolean collisionOn = false;

    /* SPRITE ATTRIBUTES */
    protected BufferedImage image;
    protected BufferedImage up1;
    protected BufferedImage up2;
    protected BufferedImage down1;
    protected BufferedImage down2;
    protected BufferedImage left1;
    protected BufferedImage left2;
    protected BufferedImage right1;
    protected BufferedImage right2;

    /* ANIMATION VALUES */
    protected int spriteNum = 1;
    protected int spriteCounter = 0;
    private int pixelCounter = 0;

    /**
     * CONSTRUCTOR
     * @param gp GamePanel
     */
    public Entity(GamePanel gp) {
        this.gp = gp;
        this.id = NEXT_ID++;
        setImages();
    }

    /* CHILD FUNCTIONS */
    /**
     * GET IMAGE
     */
    protected void setImages() { }

    /**
     * SETUP IMAGE
     * @param imagePath Path to image file
     * @return Scaled image
     */
    protected BufferedImage setupImage(String imagePath) {

        BufferedImage image = null;

        try {
            image = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream(imagePath + ".png")
            ));
            image = GamePanel.utility.scaleImage(image, gp.tileSize, gp.tileSize);
        }
        catch (IOException e) {
            System.out.println("Error loading image:" + e.getMessage());
        }

        return image;
    }

    public boolean has(Property p) {
        return properties.contains(p);
    }
    public boolean isSameFloat(Entity other) {
        return ((other.has(Property.FLOAT) && has(Property.FLOAT)) || (!other.has(Property.FLOAT) && !has(Property.FLOAT)));
    }

    /**
     * UPDATE
     * Updates the entity
     * Called every frame by GamePanel
     */
    public void update() {
        if (moving) {
            moveATile();
        }
        else if (reversing) {
            moveBackwards();
        }
    }

    /**
     * MOVE A TILE
     * Moves the entity one tile if able
     * Called by update() if the entity is moving
     */
    private void moveATile() {
        switch (direction) {
            case UP -> worldY -= speed;
            case DOWN -> worldY += speed;
            case LEFT -> worldX -= speed;
            case RIGHT-> worldX += speed;
        }

        if (this instanceof CharacterEntity) {
            cycleSprites();
        }

        pixelCounter += speed;
        if (pixelCounter >= gp.tileSize) {
            resetMovement();
            checkEntities();
        }
    }
    private void moveBackwards() {

        if (previousWorldX > worldX) worldX += speed;
        else if (previousWorldX < worldX) worldX -= speed;
        else if (previousWorldY > worldY) worldY += speed;
        else if (previousWorldY < worldY) worldY -= speed;

        if (this instanceof CharacterEntity) {
            cycleSprites();
        }

        pixelCounter += speed;
        if (pixelCounter >= gp.tileSize) {
            resetMovement();
            checkEntities();
        }
    }

    public void checkEntities() {
        Entity e = gp.cChecker.checkEntity(this, gp.entities);

        if (e != null) {
            onTouch(e);
            e.onTouch(this);
        }
    }
    private void onTouch(Entity other) {
        for (Property p : properties) {
            p.onTouch(this, other);
        }
    }

    /**
     * RESET MOVEMENT
     * Resets all values when the entity is done moving
     * Called by moving()
     */
    private void resetMovement() {
        moving = false;
        reversing = false;
        pixelCounter = 0;
        spriteNum = 1;
        spriteCounter = 0;
        collisionOn = false;

        // Only check rules when Word is moved
        if (this instanceof WordEntity) {
            gp.rulesCheck = true;
        }
    }

    /**
     * CAN MOVE
     * Checks if the entity is able to move a tile
     * Called by move()
     * @param entity Entity that wants to move
     * @param dir The direction the entity is moving
     * @return True if able to move, false if not
     */
    public boolean canMove(Entity entity, GamePanel.Direction dir, Set<Entity> moveSet) {

        if (gp.cChecker.isOutOfBounds(entity, dir)) {
            return false;
        }

        // Get all entities sitting on the next tile
        List<Entity> stack = gp.cChecker.getEntitiesAtNextTile(entity, dir);
        for (Entity e : stack) {

            // Can't move
            if (e.blocks(entity, dir)) {
                return false;
            }

            // Entity has PUSH, attempt to move
            if (e.canBePushed()) {

                // Can't move
                if (!canMove(e, dir, moveSet)) {
                    return false;
                }

                moveSet.add(e);
            }
        }

        return true;
    }
    private boolean blocks(Entity mover, GamePanel.Direction dir) {
        for (Property p : properties) {
            if (p.blocksMovement(this, mover, dir)) {
                return true;
            }
        }
        return false;
    }
    private boolean canBePushed() {
        if (this instanceof WordEntity) {
            return true;
        }

        for (Property p : properties) {
            if (p.allowsPush()) {
                return true;
            }
        }

        return false;
    }

    public void move(GamePanel.Direction dir) {
        this.direction = dir;
        this.moving = true;
    }
    private void kill() {
        if (!alive) return;

        boolean stayAlive = false;

        if (!heldEntities.isEmpty()) {
            for (Entity e : heldEntities) {
                if (e.getName().equals(name)) {
                    stayAlive = true;
                }
                else {
                    e.setWorldX(worldX);
                    e.setWorldY(worldY);
                    gp.spawnQueue.add(e);
                }
            }
        }

        if (stayAlive) return;

        alive = false;
        resetMovement();
    }
    public void transform(Entity newForm) {

        newForm.setWorldX(worldX);
        newForm.setWorldY(worldY);
        gp.spawnQueue.add(newForm);

        alive = false;
        resetMovement();
    }

    /**
     * CYCLE SPRITES
     * Changes the animation counter for draw to render the correct sprite
     */
    private void cycleSprites() {
        if (pixelCounter > 0 && pixelCounter < gp.tileSize) {
            spriteNum = 2;
        }
        else  {
            spriteNum = 1;
        }
    }

    /**
     * DRAW
     * Draws the sprite data to the graphics
     * @param g2 GamePanel
     */
    public void draw(Graphics2D g2) {

        // Match image to sprite direction
        image = getSprite();

        // Draw sprite
        g2.drawImage(image, worldX, worldY, null);
    }

    /** GET CURRENT SPRITE TO DRAW **/
    private BufferedImage getSprite() {
        BufferedImage sprite;

        if (spriteNum == 1) {
            sprite = switch (direction) {
                case UP -> up1;
                case DOWN -> down1;
                case LEFT -> left1;
                case RIGHT -> right1;
            };
        }
        else {
            sprite = switch (direction) {
                case UP -> up2;
                case DOWN -> down2;
                case LEFT -> left2;
                case RIGHT -> right2;
            };
        }

        return sprite;
    }

    public void addProperty(Property property) {
        properties.add(property);
    }
    public void clearProperties() {
        properties.clear();
        heldEntities.clear();
        lockTransformation = false;
    }

    private void playSE(int category, int record) {
        gp.playSE(category, record);
    }

    /* GETTERS AND SETTERS */
    public int getId() {
        return id;
    }

    public int getWorldX() {
        return worldX;
    }
    public int getWorldY() {
        return worldY;
    }
    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }
    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    public void setPreviousWorldX(int previousWorldX) {
        this.previousWorldX = previousWorldX;
    }
    public void setPreviousWorldY(int previousWorldY) {
        this.previousWorldY = previousWorldY;
    }

    public String getName() {
        return name;
    }
    public int getOri() {
        return ori;
    }
    public int getSide() {
        return side;
    }

    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean getMoving() {
        return moving;
    }
    public boolean getReversing() {
        return reversing;
    }
    public void  setReversing(boolean reversing) {
        this.reversing = reversing;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
    public boolean getCollisionOn() {
        return collisionOn;
    }
    public void setCollision(boolean collisionOn) {
        this.collisionOn = collisionOn;
    }

    public boolean getAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void giveHeldEntity(Entity heldEntity) {
        this.heldEntities.add(heldEntity);
    }

    public boolean getTransformationLock() {
        return lockTransformation;
    }
    public void setTransformationLock(boolean lockTransformation) {
        this.lockTransformation = lockTransformation;
    }

    public BufferedImage getImage() {
        return image;
    }
}
