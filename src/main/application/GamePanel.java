package application;

import data.EntityGenerator;
import data.SaveLoad;
import entity.Entity;
import tile.TileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Set;

import static application.GamePanel.Direction.*;
import static application.GamePanel.Direction.RIGHT;

public class GamePanel extends JPanel implements Runnable {

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    /* GENERAL CONFIG */
    private Graphics2D g2;
    private Thread gameThread;
    public static UtilityTool utility = new UtilityTool();

    /* CONTROLS / SOUND / UI */
    public KeyHandler keyH = new KeyHandler(this);
    public UI ui = new UI(this);

    /* SCREEN SETTINGS */
    private final int originalTileSize = 16; // 16x16 tile
    private final int scale = 3; // scale rate to accommodate for large screen
    public final int tileSize = originalTileSize * scale; // scaled tile (16*3 = 48px)
    public final int maxScreenCol = 33; // columns (width)
    public final int maxScreenRow = 18; // rows (height)
    public final int screenWidth = tileSize * maxScreenCol; // screen width (33 x 48: 1584px)
    public final int screenHeight = tileSize * maxScreenRow; // screen height (18 x 48: 864px)

    /* WORLD SIZE */
    public int maxWorldCol = 33;
    public int maxWorldRow = 18;

    /* MAPS */
    public final String[] lvlFiles = {"lvl_1.txt"};
    public final int maxLvls = lvlFiles.length;
    public int currentLvl = 0;

    /* FULL SCREEN SETTINGS */
    public boolean fullScreenOn = false;
    private int screenWidth2 = screenWidth;
    private int screenHeight2 = screenHeight;
    private BufferedImage tempScreen;

    /* GAME STATES */
    public int gameState;
    public final int playState = 1;
    public final int editState = 2;

    /* HANDLERS */
    public TileManager tileM = new TileManager(this);
    public AssetSetter aSetter = new AssetSetter(this);
    public CollisionChecker cChecker = new CollisionChecker(this);
    public final LogicHandler lHandler = new LogicHandler(this);
    public final EntityGenerator eGenerator = new EntityGenerator(this);
    public final SaveLoad dataHandler = new SaveLoad(this);

    /* ENTITIES */
    public Entity[][] chr = new Entity[maxLvls][50];
    public Entity[][] obj = new Entity[maxLvls][50];
    public Entity[][] words = new Entity[maxLvls][50];
    public Entity[][] iTiles = new Entity[maxLvls][100];

    /* GENERAL VALUES */
    public boolean showGrid = true;
    public boolean canLoad = false;
    public boolean rulesCheck = false;
    public boolean win = false;

    private int cooldown = 0;

    /**
     * CONSTRUCTOR
     */
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // screen size
        this.setBackground(Color.black);
        this.setDoubleBuffered(true); // improves rendering performance

        this.addKeyListener(keyH);
        this.setFocusable(true); // GamePanel in focus to receive input
    }

    /**
     * SETUP GAME
     * Prepares the game with default settings
     * Called by Driver
     */
    protected void setupGame() {

        // Temp game window (before drawing to window)
        tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) tempScreen.getGraphics();

        gameState = editState;
        setupLevel();

        if (fullScreenOn) {
            setFullScreen();
        }
    }

    /**
     * SET FULL SCREEN
     * Changes the graphics to full screen mode
     * Called by setupGame()
     */
    private void setFullScreen() {

        // Get system screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(Driver.window);

        // Get full screen width and height
        screenWidth2 = Driver.window.getWidth();
        screenHeight2 = Driver.window.getHeight();
    }

    /**
     * START GAME THREAD
     * Runs a new thread
     * Called by Driver
     */
    protected void startGameThread() {

        // New Thread with GamePanel class
        gameThread = new Thread(this);

        // Calls run() endlessly
        gameThread.start();
    }

    /**
     * RUN
     * Draws and updates the game 60 times a second
     * Called using the game thread start() method
     */
    @Override
    public void run() {

        long currentTime;
        long lastTime = System.nanoTime();
        double drawInterval = 1000000000.0 / 60.0; // 1/60th of a second
        double delta = 0;

        // Update and repaint gameThread
        while (gameThread != null) {

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval; // Time passed (1/60th second)
            lastTime = currentTime;

            if (delta >= 1) {

                // Update game information
                update();

                // Draw temp screen with new information
                drawToTempScreen();

                // Send temp screen to monitors
                drawToScreen();

                delta = 0;
            }
        }
    }

    /**
     * UPDATE
     * Runs each time the frame is updated
     * Called by run()
     */
    private void update() {
        if (gameState == playState) {
            updateEntities();
            handleMovementInput();
            checkLoad();
            checkRules();
            checkWin();

            if (keyH.yPressed) {
                keyH.yPressed = false;
                showGrid = !showGrid;
            }

            if (keyH.startPressed) {
                keyH.startPressed = false;
                showGrid = true;
                gameState = editState;
            }
        }
        else if (gameState == editState) {
            if (keyH.startPressed) {
                keyH.startPressed = false;
                gameState = playState;
                setupLevel();
            }
        }
    }

    /**
     * UPDATE ENTITIES
     * Iterates over each entity and updates if alive, sets to null if not
     * Called by runUpdate()
     */
    private void updateEntities() {
        for (Entity[] entities : getAllEntities()) {
            for (int i = 0; i < entities.length; i++) {
                if (entities[i] == null) continue;

                if (entities[i].alive) {
                    entities[i].update();
                }
                else {
                    entities[i] = null;
                }
            }
        }
    }

    /**
     * HANDLE MOVEMENT INPUT
     */
    private void handleMovementInput() {

        // Entities currently moving, do nothing
        if (!noEntitiesMoving()) {
            return;
        }

        // Can load if no movement
        canLoad = true;

        // 3-frame buffer for tile movement
        cooldown++;

        // Get direction user pressed (null if none)
        Direction directionPressed = getPressedDirection();

        // Arrow pressed while no entity movement
        if (directionPressed != null && cooldown > 2 && hasMoveableEntities(directionPressed)) {

            dataHandler.saveState();
            canLoad = false;
            cooldown = 0;

            for (Entity[] entities : getAllEntities()) {
                moveEntities(entities, directionPressed);
            }
        }
    }
    private boolean noEntitiesMoving() {
        for (Entity[] entities : getAllEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;

                if (e.moving || e.reversing) {
                    return false;
                }
            }
        }

        return true;
    }
    private Direction getPressedDirection() {

        Direction direction = null;

        if (keyH.upPressed) direction = UP;
        else if (keyH.downPressed) direction = DOWN;
        else if (keyH.leftPressed) direction = LEFT;
        else if (keyH.rightPressed) direction = RIGHT;

        return direction;
    }
    private boolean hasMoveableEntities(Direction direction) {
        for (Entity[] entities : getAllEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;

                if (e.properties.contains(Entity.Property.YOU) && e.canMove(e, direction, new LinkedHashSet<>())) {
                    return true;
                }
            }
        }

        return false;
    }
    private void moveEntities(Entity[] entities, Direction direction) {

        // Set of entities to move
        Set<Entity> moveSet = new LinkedHashSet<>();

        // Loop through each entity
        for (Entity e : entities) {
            if (e == null) continue;

            // Entity not YOU or unable to move
            if (!e.properties.contains(Entity.Property.YOU) || !e.canMove(e, direction, moveSet)) {
                continue;
            }

            moveSet.add(e);
        }

        // Start move for each entity that can move
        for (Entity m : moveSet) {
            m.startMove(direction);
        }
    }

    /**
     * CHECK LOAD
     * Calls dataHandler to load entity states if B pressed
     * Can only call redo when canLoad is TRUE and no one moving
     * Called by update()
     */
    private void checkLoad() {
        if (keyH.bPressed && canLoad && noEntitiesMoving()) {
            keyH.bPressed = false;
            dataHandler.loadState();
            rulesCheck = true;
        }
    }

    /**
     * CHECK RULES
     * Calls lHandler to check rules if rulesCheck is TRUE
     * Called by update()
     */
    private void checkRules() {

        // Checks rules once per update if turned on by an entity
        // Wait until entities stop moving
        if (rulesCheck && noEntitiesMoving()) {
            lHandler.scanForRules();
            rulesCheck = false;
        }
    }

    /**
     * CHECK WIN
     * Checks if any entity has YOU and WIN,
     *  sets up the next level if found
     * Called by update()
     */
    private void checkWin() {
        for (Entity[] entities : getAllRegularEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;
                e.checkWin(e);
            }
        }
    }

    /**
     * DRAW TO TEMP SCREEN
     * Draws to temporary screen before drawing to front-end
     * Called by run()
     */
    private void drawToTempScreen() {
        clearBackBuffer();
        tileM.draw(g2);
        drawEntities();
        ui.draw(g2);
    }

    /**
     * CLEAR BACK BUFFER
     * Fills the background with black to eliminate artifacting
     * Called by drawToTempScreen()
     */
    private void clearBackBuffer() {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    /**
     * Iterates over each entity and calls draw method
     * Called by drawToTempScreen()
     */
    private void drawEntities() {
        for (Entity[] entities : getAllEntities()) {
            for (Entity e : entities) {
                if (e == null) continue;

                e.draw(g2);
            }
        }
    }

    /**
     * DRAW TO SCREEN
     * Draws graphics to screen
     * Called by run()
     */
    private void drawToScreen() {
        Graphics g = getGraphics();
        g.drawImage(tempScreen, 0, 0, screenWidth2, screenHeight2, null);
        g.dispose();
    }

    /**
     * RESET LEVEL
     * Resets the current level to starting position
     * Called by KeyHandler
     */
    public void setupLevel() {
        win = false;
        dataHandler.clearData();
        tileM.loadLvl();
        aSetter.setup();
        lHandler.scanForRules();
        ui.fetchEntity();
    }

    public Entity[][] getAllEntities() {
        return new Entity[][]{iTiles[currentLvl], obj[currentLvl], chr[currentLvl], words[currentLvl]};
    }
    public Entity[][] getAllRegularEntities() {
        return new Entity[][] { iTiles[currentLvl], obj[currentLvl], chr[currentLvl] };
    }

    public Entity[] getEntityList(int index) {
        if (index <= 0) return words[0];
        else if (index == 1) return iTiles[0];
        else if (index == 2) return obj[0];
        else return chr[0];
    }
}
