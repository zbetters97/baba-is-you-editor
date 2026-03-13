package entity;

import application.GamePanel;
import application.GamePanel.Direction;
import entity.word.*;
import rules.Properties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static application.GamePanel.Direction.*;

public class Entity {

    // Empty enum list to hold properties
    private final EnumSet<Properties> properties = EnumSet.noneOf(Properties.class);

    protected GamePanel gp;

    private static int NEXT_ID = 0;
    private int id;

    /* GENERAL ATTRIBUTES */
    protected Point point, previousPoint;
    protected String name;
    protected int ori, side;
    private boolean alive = true;

    /* MOVEMENT VALUES */
    protected GamePanel.Direction direction = DOWN;
    private final int speed = 6;
    private boolean moving = false;
    private boolean reversing = false;
    private final List<Entity> heldEntities = new ArrayList<>();
    private boolean lockTransformation = false;

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

    public boolean isSameFloat(Entity other) {
        return ((other.has(Properties.FLOAT) && has(Properties.FLOAT)) || (!other.has(Properties.FLOAT) && !has(Properties.FLOAT)));
    }

    public boolean has(Properties p) {
        return properties.contains(p);
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
            case UP -> point.y -= speed;
            case DOWN -> point.y += speed;
            case LEFT -> point.x -= speed;
            case RIGHT-> point.x += speed;
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

        if (previousPoint.x > point.x) point.x += speed;
        else if (previousPoint.x < point.x) point.x -= speed;
        else if (previousPoint.y > point.y) point.y += speed;
        else if (previousPoint.y < point.y) point.y -= speed;

        if (this instanceof CharacterEntity) {
            cycleSprites();
        }

        pixelCounter += speed;
        if (pixelCounter >= gp.tileSize) {
            resetMovement();
        }
    }

    public void checkEntities() {
        if (has(Properties.DEFEAT) && has(Properties.YOU)) {
            kill();
            return;
        }

        ArrayList<Entity> targets = gp.cChecker.checkEntity(this);

        if (targets.isEmpty()) return;

        for (Entity e : targets) {
            e.onTouch(this);
        }
    }
    private void onTouch(Entity other) {
        for (Properties p : properties) {
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

        if (this instanceof WordEntity) {
            gp.wordMoved = true;
        }
    }

    /**
     * CANT MOVE
     * Checks if the entity is able to move a tile
     * Called by move()
     * @param entity Entity that wants to move
     * @param dir The direction the entity is moving
     * @return True if not able to move, false if able
     */
    public boolean cantMove(Entity entity, GamePanel.Direction dir, Set<Entity> moveSet) {

        if (gp.cChecker.isOutOfBounds(entity, dir)) {
            return true;
        }

        // Get all entities sitting on the next tile
        List<Entity> stack = gp.cChecker.getEntitiesAtNextTile(entity, dir);
        for (Entity e : stack) {

            // Can't move
            if (e.blocks(entity, dir)) {
                return true;
            }

            // Entity has PUSH, attempt to move
            if (e.canBePushed()) {

                // Can't move
                if (cantMove(e, dir, moveSet)) {
                    return true;
                }

                moveSet.add(e);
            }
        }

        return false;
    }
    private boolean blocks(Entity mover, GamePanel.Direction dir) {
        for (Properties p : properties) {
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

        for (Properties p : properties) {
            if (p.allowsPush(this)) {
                return true;
            }
        }

        return false;
    }

    public void move(GamePanel.Direction dir) {
        this.direction = dir;
        this.moving = true;
        setPreviousPoint(point);
    }
    public void kill() {
        if (!alive) return;

        boolean stayAlive = false;

        if (!heldEntities.isEmpty()) {
            for (Entity e : heldEntities) {
                if (e.getName().equals(name)) {
                    stayAlive = true;
                }
                else {
                    e.setPoint(point);
                    gp.spawnQueue.add(e);
                }
            }
        }

        if (stayAlive) return;

        alive = false;
        resetMovement();
    }
    public void transform(Entity newForm) {
        newForm.setPoint(point);
        gp.spawnQueue.add(newForm);

        alive = false;
        resetMovement();
    }
    public void win() {
        gp.win = true;
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
        g2.drawImage(image, point.x, point.y, null);
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

    public void addProperty(Properties property) {
        properties.add(property);
    }
    public void clearProperties() {
        properties.clear();
    }

    public void playSE(int category, int record) {
        gp.playSE(category, record);
    }

    /* GETTERS AND SETTERS */
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Point getPoint() {
        return point;
    }
    public void setPoint(Point point) {
        this.point = new Point(point);
    }

    public Point getPreviousPoint() {
        return previousPoint;
    }
    public void setPreviousPoint(Point previousPoint) {
        this.previousPoint = new Point(previousPoint);
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

    public boolean getAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void giveHeldEntity(Entity heldEntity) {
        this.heldEntities.add(heldEntity);
    }
    public List<Entity> getHeldEntities() {
        return heldEntities;
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
