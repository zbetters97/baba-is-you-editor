package entity;

import application.GamePanel;
import entity.character.CHR_Baba;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static application.GamePanel.Direction.*;

public class Entity {

    // Properties an object can have
    public enum Property {
        WIN,
        STOP,
        PUSH,
        YOU,
        DEFEAT,
        SINK,
    }

    // Empty enum list to hold properties
    public EnumSet<Entity.Property> properties = EnumSet.noneOf(Entity.Property.class);

    protected GamePanel gp;

    /* GENERAL ATTRIBUTES */
    public int worldX, worldY;
    public int previousWorldX, previousWorldY;
    public String name;
    public boolean alive = true;

    /* MOVEMENT VALUES */
    public GamePanel.Direction direction = DOWN;
    public int speed = 4;
    public boolean moving = false;
    public boolean reversing = false;

    /* ANIMATION VALUES */
    protected int pixelCounter = 0;

    /* COLLISION VALUES */
    public Rectangle hitbox = new Rectangle(0, 0, 48, 48);
    public boolean collisionOn = false;

    /* MISC VALUES */
    public int ori, side;

    /* SPRITE ATTRIBUTES */
    public BufferedImage image;
    protected BufferedImage up1;
    protected BufferedImage up2;
    protected BufferedImage down1;
    protected BufferedImage down2;
    protected BufferedImage left1;
    protected BufferedImage left2;
    protected BufferedImage right1;
    protected BufferedImage right2;
    protected int spriteNum = 1;
    protected int spriteCounter = 0;

    /**
     * CONSTRUCTOR
     * @param gp GamePanel
     */
    public Entity(GamePanel gp) {
        this.gp = gp;
        getImages();
    }

    /* CHILD FUNCTIONS */
    /**
     * GET IMAGE
     */
    protected void getImages() { }

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

        if (this instanceof CHR_Baba) {
            cycleSprites();
        }

        pixelCounter += speed;
        if (pixelCounter >= gp.tileSize) {
            resetMovement();
            checkRules();
        }
    }

    private void moveBackwards() {

        if (previousWorldX > worldX) worldX += speed;
        else if (previousWorldX < worldX) worldX -= speed;
        else if (previousWorldY > worldY) worldY += speed;
        else if (previousWorldY < worldY) worldY -= speed;

        if (this instanceof CHR_Baba) {
            cycleSprites();
        }

        pixelCounter += speed;
        if (pixelCounter >= gp.tileSize) {
            resetMovement();
            checkRules();
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
        gp.rulesCheck = true;
    }

    /**
     * CHECK RULES
     * Checks various rules in play on each entity list
     * Called by pushEntities()
     */
    private void checkRules() {
        checkEntities(gp.words);
        checkEntities(gp.obj);
        checkEntities(gp.iTiles);
        checkEntities(gp.chr);
    }

    public void startMove(GamePanel.Direction dir) {
        this.direction = dir;
        this.moving = true;
    }

    /**
     * CANT MOVE
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

            // Can't move, entity has STOP
            if (e.properties.contains(Property.STOP)) {
                return false;
            }

            // Entity has PUSH, attempt to move
            if (e.properties.contains(Property.PUSH)) {

                // Can't move
                if (!canMove(e, dir, moveSet)) {
                    return false;
                }

                moveSet.add(e);
            }
        }

        return true;
    }

    /**
     * CHECK ENTITIES
     * Checks each type of collision for given entity list
     * @param entities List of entities to check collision against
     */
    private void checkEntities(Entity[] entities) {
        int ent = gp.cChecker.checkEntity(this, entities);

        if (ent != -1) {
            checkSink(entities[ent]);
            checkWin(entities[ent]);
            checkDefeat(entities[ent]);
            checkWin(entities[ent]);
        }
    }

    /**
     * CHECK SINK
     * Sets alive to false if the object has SINK
     * Called by checkEntities()
     */
    private void checkSink(Entity obj) {
        if (obj.properties.contains(Property.SINK) && !obj.properties.contains(Property.STOP)) {
            alive = false;
            resetMovement();

            obj.alive = false;
            obj.resetMovement();
        }
    }

    /**
     * CHECK DEFEAT
     * Sets alive to false if the object has DEFEAT
     * Called by checkEntities(0
     */
    private void checkDefeat(Entity obj) {
        if (obj.properties.contains(Property.DEFEAT) && !obj.properties.contains(Property.STOP)) {
            alive = false;
            resetMovement();
        }
    }

    /**
     * CHECK WIN
     * Checks if the entity can win the game/level
     * Entity needs to be controlled by player to win
     */
    public void checkWin(Entity obj) {
        if (properties.contains(Property.YOU) && obj.properties.contains(Property.WIN)) {
            gp.win = true;
        }
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
     * SET FORM
     * Changes the entity's properties to match the new form
     */
    public void setForm(Entity newForm) {
        collisionOn = false;

        // Copy all attributes from new form
        name = newForm.name;
        properties = newForm.properties;
        up1 = newForm.up1;
        up2 = newForm.up2;
        down1 = newForm.down1;
        down2 = newForm.down2;
        left1 = newForm.left1;
        left2 = newForm.left2;
        right1 = newForm.right1;
        right2 = newForm.right2;
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
}
