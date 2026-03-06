package application;

import data.*;
import entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import static application.GamePanel.Direction.*;
import static application.GamePanel.Direction.RIGHT;
import static entity.Entity.Property.YOU;

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

    /* UTILITIES / UI */
    public static UtilityTool utility = new UtilityTool();
    public KeyHandler keyH = new KeyHandler();
    public final EntityGenerator eGenerator = new EntityGenerator(this);
    private final SoundCard music = new SoundCard();
    private final SoundCard se = new SoundCard();
    private final UI ui = new UI(this);

    /* SCREEN SETTINGS */
    private final int originalTileSize = 16; // 16x16 tile
    private final int scale = 3; // scale rate to accommodate for large screen
    public final int tileSize = originalTileSize * scale; // scaled tile (16*3 = 48px)
    private final int maxScreenCol = 33; // columns (width)
    private final int maxScreenRow = 18; // rows (height)
    public final int screenWidth = tileSize * maxScreenCol; // screen width (33 x 48: 1584px)
    public final int screenHeight = tileSize * maxScreenRow; // screen height (18 x 48: 864px)

    /* WORLD SIZE */
    public int maxWorldCol = 33;
    public int maxWorldRow = 18;

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
    public CollisionChecker cChecker = new CollisionChecker(this);
    private final LogicHandler lHandler = new LogicHandler(this);
    private final StateHandler stateHandler = new StateHandler(this);

    public Firebase db = new Firebase(this);
    public boolean dbConnected = false;
    public SaveLoad saveLoad = new SaveLoad(this);
    public DataStorage levelProgress = new DataStorage();
    public Map<String, String> saveFiles = new LinkedHashMap<>();
    public String account = "steelpro43";
    public String levelPath = "levels/" + account + "/";
    public boolean isUploading = false;

    /* ENTITIES */
    public ArrayList<Entity> entities = new ArrayList<>();
    public ArrayList<Entity> spawnQueue = new ArrayList<>();

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
    protected void setupGame()  {

        // Temp game window (before drawing to window)
        tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) tempScreen.getGraphics();

        // Connect to Firebase
        dbConnected = db.init();

        // Retrieve save files from Firebase (K: file name, V: created date)
        if (dbConnected) {
            saveFiles = db.getSaveFileNames();
        }

        gameState = editState;
        playMusic(0, 0);

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
            checkRedo();
            checkRules();
            checkWin();
            handleKeyPress();
        }
    }

    /**
     * UPDATE ENTITIES
     * Iterates over each entity and updates if alive, sets to null if not
     * Called by runUpdate()
     */
    private void updateEntities() {
        for (Entity e : entities) e.update();
        entities.removeIf(e -> !e.getAlive());

        // Spawn new entities after update
        // Check rules if new entities spawned in
        if (!spawnQueue.isEmpty()) {
            rulesCheck = true;
            entities.addAll(spawnQueue);
            spawnQueue.clear();
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
        if (directionPressed != null && cooldown > 3 && hasMoveableEntities(directionPressed)) {

            stateHandler.saveState();
            canLoad = false;
            cooldown = 0;

            moveEntities(entities, directionPressed);
        }
    }
    private boolean noEntitiesMoving() {
        for (Entity e : entities) {
            if (e.getMoving() || e.getReversing()) {
                return false;
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
        for (Entity e : entities) {
            if (e.has(YOU) && e.canMove(e, direction, new LinkedHashSet<>())) {
                return true;
            }
        }

        return false;
    }
    private void moveEntities(ArrayList<Entity> entities, Direction direction) {

        // Set of entities to move
        Set<Entity> moveSet = new LinkedHashSet<>();

        // Loop through each entity
        for (Entity e : entities) {

            // Entity not YOU or unable to move
            if (!e.has(YOU) || !e.canMove(e, direction, moveSet)) {
                continue;
            }

            moveSet.add(e);
        }

        // Start move for each entity that can move
        playSE(2, 0);
        for (Entity m : moveSet) {
            m.move(direction);
        }
    }

    private void checkRedo() {
        if (keyH.bPressed && canLoad && noEntitiesMoving()) {
            keyH.bPressed = false;
            playSE(2, 0);
            stateHandler.loadState();

            if (noEntitiesMoving()) {
                rulesCheck = true;
            }
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

    private void checkWin() {
        if (win && isUploading) {
            win = false;
            isUploading = false;

            saveLoad.loadFromData();
            showGrid = true;
            gameState = editState;
            ui.subState = 2;
        }
    }

    private void handleKeyPress() {
        if (keyH.yPressed) {
            keyH.yPressed = false;
            showGrid = !showGrid;
        }
        else if (keyH.startPressed) {
            keyH.startPressed = false;

            isUploading = false;
            saveLoad.loadFromData();
            showGrid = true;
            gameState = editState;
        }
    }

    /**
     * DRAW TO TEMP SCREEN
     * Draws to temporary screen before drawing to front-end
     * Called by run()
     */
    private void drawToTempScreen() {
        clearBackBuffer();
        drawGrid();
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

    private void drawGrid() {
        // Semi-transparent white
        g2.setColor(new Color(255, 255, 255, 50));
        g2.setStroke(new BasicStroke(1));

        // Vertical lines
        for (int col = 0; col <= maxWorldCol; col++) {
            int x = col * tileSize;
            g2.drawLine(x, 0, x, maxWorldRow * tileSize);
        }

        // Horizontal lines
        for (int row = 0; row <= maxWorldRow; row++) {
            int y = row * tileSize;
            g2.drawLine(0, y, maxWorldCol * tileSize, y);
        }
    }

    /**
     * Iterates over each entity and calls draw method
     * Called by drawToTempScreen()
     */
    private void drawEntities() {
        for (Entity e : entities) {
            e.draw(g2);
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
        stopMusic();
        win = false;
        stateHandler.clearData();
        lHandler.scanForRules();
        ui.editing_GetEntity();
        playMusic(0, 1);
    }

    public void playSE(int category, int record) {
        se.setFile(category, record);
        se.play();
    }

    public void playMusic(int category, int record) {
        int loopStart = music.getLoopStart(category, record);
        music.setFile(category, record);
        music.loop(loopStart);
    }
    public void stopMusic() {
        music.stop();
    }
}
