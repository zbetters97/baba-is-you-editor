package application;

import entity.Entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UI {

    /* CONFIG */
    private final GamePanel gp;
    private Graphics2D g2;

    private int subState = 0;

    /* CURSOR VALUES */
    private final BufferedImage cursor;
    private final BufferedImage cursor_select;
    private int slotCol = 0;
    private int slotRow = 0;

    /* ASSET HANDLERS */
    private int entityListIndex = 0;
    private int entityIndex = 0;
    private Entity[] currentEntityList;
    private Entity currentEntity;
    private Entity[] selectedEntityList;
    private Entity selectedEntity;
    private final ArrayList<ArrayList<String>> entityNames = new ArrayList<>();

    /**
     * CONSTRUCTOR
     * Instance created by GamePanel
     * @param gp GamePanel
     */
    public UI(GamePanel gp) {
        this.gp = gp;

        cursor = setup("/ui/ui_cursor");
        cursor_select = setup("/ui/ui_cursor_select");

        entityNames.addAll(Arrays.asList(
                new ArrayList<>(Arrays.asList(
                        "WORD_BABA", "WORD_DEFEAT", "WORD_FLAG",
                        "WORD_IS", "WORD_PUSH","WORD_ROCK",
                        "WORD_SINK","WORD_SKULL", "WORD_STOP",
                        "WORD_WALL", "WORD_WATER","WORD_WIN",
                        "WORD_YOU"
                )),
                new ArrayList<>(Arrays.asList("WALL", "WATER")),
                new ArrayList<>(Arrays.asList("FLAG", "ROCK", "SKULL")),
                new ArrayList<>(List.of("BABA"))
        ));
    }

    public void setupUI() {
        fetchEntityList();
        fetchEntity();
    }

    /**
     * DRAW
     * Draws the UI
     * Called by GamePanel
     * @param g2 Graphics2D engine
     */
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        if (gp.gameState == gp.editState) {
            drawEdit();
        }
        else if (gp.gameState == gp.playState) {
            drawHUD();
        }
    }

    private void drawEdit() {

        if (subState == 0) {
            drawCurrentEntity();

            // Entity currently selected, draw sprite under cursor
            if (selectedEntity != null) {
                g2.drawImage(selectedEntity.image, slotCol, slotRow, gp.tileSize, gp.tileSize, null);
                g2.drawImage(cursor_select, slotCol, slotRow, gp.tileSize, gp.tileSize, null);
            }
            else {
                g2.drawImage(cursor, slotCol, slotRow, gp.tileSize, gp.tileSize, null);
            }

            if (gp.keyH.yPressed) {
                gp.keyH.yPressed = false;
                subState = 1;
            }

            handleCursorAPress();
            handleCursorBPress();
            handleCursorDirectionPress();
        }
        else {
            drawEntitiesMenu();
            handleEntityDirectionPress();

            if (gp.keyH.yPressed || gp.keyH.aPressed || gp.keyH.bPressed) {
                gp.keyH.yPressed = false;
                gp.keyH.aPressed = false;
                gp.keyH.bPressed = false;
                subState = 0;
                fetchEntity();
                fetchEntityList();
            }
        }
    }
    private void drawCurrentEntity() {
        int x = gp.screenWidth - gp.tileSize;
        int y = 0;

        g2.drawImage(currentEntity.image, x, y, gp.tileSize, gp.tileSize, null);
    }

    private void handleCursorAPress() {
        if (gp.keyH.aPressed) {
            gp.keyH.aPressed = false;

            // Cursor on existing entity and grabbed, return
            if (entityGrabbed()) return;

            // Entity currently grabbed, place down
            if (selectedEntity != null) {
                placeEntity(selectedEntity, selectedEntityList);
                selectedEntity = null;
                selectedEntityList = null;
            }
            // Not currently holding entity, place down new one
            else {
                Entity newEntity = gp.eGenerator.getEntity(currentEntity.name);
                placeEntity(newEntity, currentEntityList);
            }
        }
    }
    private boolean entityGrabbed() {
        for (Entity[] entities : gp.getAllEntities()) {
            for (int i = 0; i < entities.length; i++) {
                if (entities[i] == null) continue;

                // Entity found at same X/Y
                if (entities[i].worldX == slotCol && entities[i].worldY == slotRow) {

                    // Trying to place selected entity on top of existing, not allowed
                    if (selectedEntity != null) return true;

                    // Grab new entity, remove from level
                    selectedEntity = entities[i];
                    selectedEntityList = entities;

                    entities[i] = null;
                    return true;
                }
            }
        }

        return false;
    }
    private void placeEntity(Entity entity, Entity[] entities) {
        for (int i = 0; i < entities.length; i++) {
            if (entities[i] == null) {

                // Find the closest available spot, place entity in list
                entity.worldX = slotCol;
                entity.worldY = slotRow;
                entities[i] = entity;

                return;
            }
        }
    }

    private void handleCursorBPress() {
        if (gp.keyH.bPressed) {
            gp.keyH.bPressed = false;

            // Find entity at X/Y
            for (Entity[] entities : gp.getAllEntities()) {
                for (int i = 0; i < entities.length; i++) {
                    if (entities[i] == null) continue;

                    // Entity found, delete from list
                    if (entities[i].worldX == slotCol && entities[i].worldY == slotRow) {
                        entities[i] = null;
                        return;
                    }
                }
            }
        }
    }
    private void handleCursorDirectionPress() {
        if (gp.keyH.upPressed) {
            gp.keyH.upPressed = false;
            if (slotRow - gp.tileSize >= 0) {
                slotRow -= gp.tileSize;
            }
        }
        else if (gp.keyH.downPressed) {
            gp.keyH.downPressed = false;
            if (slotRow + gp.tileSize <= gp.screenHeight - gp.tileSize) {
                slotRow += gp.tileSize;
            }
        }
        else if (gp.keyH.leftPressed) {
            gp.keyH.leftPressed = false;
            if (slotCol - gp.tileSize >= 0) {
                slotCol -= gp.tileSize;
            }
        }
        else if (gp.keyH.rightPressed) {
            gp.keyH.rightPressed = false;
            if (slotCol + gp.tileSize <= gp.screenWidth - gp.tileSize) {
                slotCol += gp.tileSize;
            }
        }
    }

    private void drawEntitiesMenu() {

        int cursorX = (gp.screenWidth / 2) - gp.tileSize;
        int cursorY = (gp.screenHeight / 2) - gp.tileSize;

        int scrollOffsetX = cursorX - (entityListIndex * gp.tileSize);
        int scrollOffsetY = cursorY - (entityIndex * gp.tileSize);

        int x, y;
        for (int i = 0; i < entityNames.size(); i++) {

            // Shift lists left or right while keeping cursor the same
            x = scrollOffsetX + (i * gp.tileSize);

            // Shift entire list down to cursor's Y if current list
            y = entityListIndex == i ? scrollOffsetY : cursorY;

            for (int c = 0; c < entityNames.get(i).size(); c++) {
                Entity e = gp.eGenerator.getEntity(entityNames.get(i).get(c));

                // Reduce transparency if not active list
                if (i != entityListIndex) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                }
                g2.drawImage(e.image, x, y, gp.tileSize, gp.tileSize, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                // Draw cursor if current selection
                if (entityListIndex == i && entityIndex == c) {
                    g2.drawImage(cursor, cursorX, cursorY, gp.tileSize, gp.tileSize, null);
                }

                y += gp.tileSize;
            }
        }
    }
    private void handleEntityDirectionPress() {
        if (gp.keyH.upPressed) {
            gp.keyH.upPressed = false;

            entityIndex--;
            if (entityIndex < 0) {
                entityIndex = entityNames.get(entityListIndex).size() - 1;
            }
        }
        else if (gp.keyH.downPressed) {
            gp.keyH.downPressed = false;

            entityIndex++;
            if (entityIndex > entityNames.get(entityListIndex).size() - 1) {
                entityIndex = 0;
            }
        }
        else if (gp.keyH.leftPressed) {
            gp.keyH.leftPressed = false;

            entityListIndex--;
            entityIndex = 0;
            if (entityListIndex < 0) {
                entityListIndex = entityNames.size() - 1;
            }
        }
        else if (gp.keyH.rightPressed) {
            gp.keyH.rightPressed = false;

            entityListIndex++;
            entityIndex = 0;
            if (entityListIndex > entityNames.size() - 1) {
                entityListIndex = 0;
            }
        }
    }
    private void fetchEntityList() {
        currentEntityList = gp.getEntityList(entityListIndex);

        String name = entityNames.get(entityListIndex).get(entityIndex);
        currentEntity = gp.eGenerator.getEntity(name);

        currentEntity.worldX = slotCol;
        currentEntity.worldY = slotRow;
    }
    private void fetchEntity() {
        String name = entityNames.get(entityListIndex).get(entityIndex);
        currentEntity = gp.eGenerator.getEntity(name);

        currentEntity.worldX = slotCol;
        currentEntity.worldY = slotRow;
    }


    /**
     * DRAW HUD
     * Draws the HUD during play state
     * called by draw()
     */
    private void drawHUD() {
        drawDebug();
    }

    /**
     * DRAW DEBUG
     * UI for debug information
     * Called by drawHUD()
     */
    private void drawDebug() {

        if (gp.chr[gp.currentLvl][0] == null) return;

        int x = 10;
        int y = gp.tileSize * 6;
        int lineHeight = 20;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));

        // Draw coordinates
        g2.drawString("Level: " + (gp.currentLvl + 1), x, y);
        y += lineHeight;
        g2.drawString("WorldX: " + gp.chr[gp.currentLvl][0].worldX, x, y);
        y += lineHeight;
        g2.drawString("WorldY: " + gp.chr[gp.currentLvl][0].worldY, x, y);
        y += lineHeight;
        g2.drawString("Column: " + gp.chr[gp.currentLvl][0].worldX / gp.tileSize, x, y);
        y += lineHeight;
        g2.drawString("Row: " + gp.chr[gp.currentLvl][0].worldY / gp.tileSize, x, y);
    }

    private BufferedImage setup(String imagePath) {

        BufferedImage image = null;

        try {
            image = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream(imagePath + ".png")
            ));
            image = GamePanel.utility.scaleImage(image, gp.tileSize, gp.tileSize);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return image;
    }
}
