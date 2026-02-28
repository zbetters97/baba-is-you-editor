package application;

import entity.Entity;
import entity.UIEntity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class UI {

    /* CONFIG */
    private final GamePanel gp;
    private Graphics2D g2;

    /* CURSOR VALUES */
    private final BufferedImage cursor;
    private final BufferedImage cursor_select;
    private int slotCol = 0;
    private int slotRow = 0;

    private int subState = 0;
    private int commandNum = 0;

    /* ASSET HANDLERS */
    private final ArrayList<ArrayList<UIEntity>> entityLibrary = new ArrayList<>();
    private int entityListIndex = 0;
    private int entityIndex = 0;
    private Entity[] currentEntityList;
    private Entity currentEntity;
    private Entity[] selectedEntityList;
    private Entity selectedEntity;

    private boolean wasYPressed = false;

    /**
     * CONSTRUCTOR
     * Instance created by GamePanel
     * @param gp GamePanel
     */
    public UI(GamePanel gp) {
        this.gp = gp;

        cursor = setup("/ui/ui_cursor");
        cursor_select = setup("/ui/ui_cursor_select");
        fillEntityLibrary();
    }

    private void fillEntityLibrary() {
        entityLibrary.addAll(Arrays.asList(
                buildEntityLibraryList(
                        "words",
                        "WORD_IS", "WORD_BABA", "WORD_FLAG", "WORD_KEKE",
                        "WORD_ROCK", "WORD_SKULL", "WORD_WALL", "WORD_WATER"
                ),
                buildEntityLibraryList(
                        "words",
                        "WORD_DEFEAT", "WORD_PUSH", "WORD_SINK",
                        "WORD_STOP", "WORD_WIN", "WORD_YOU"
                ),
                buildITilesLibraryList(),
                buildEntityLibraryList(
                        "objects",
                        "FLAG", "ROCK", "SKULL"
                ),
                buildEntityLibraryList(
                        "characters",
                        "BABA", "KEKE"
                )
        ));
    }
    private ArrayList<UIEntity> buildEntityLibraryList(String path, String... names) {
        ArrayList<UIEntity> list = new ArrayList<>();

        for (String name : names) {
            list.add(new UIEntity(name, path, gp));
        }

        return list;
    }
    private ArrayList<UIEntity> buildITilesLibraryList() {
        ArrayList<UIEntity> i_tiles = new ArrayList<>();

        i_tiles.add(new UIEntity("WALL", 0, 0, "i_tiles", gp));

        for (int i = 1; i < 3; i++) {
            for (int c = 0; c < 3; c++) {
                i_tiles.add(new UIEntity("WALL", i, c, "i_tiles", gp));
            }
        }
        for (int i = 0; i < 4; i++) {
            i_tiles.add(new UIEntity("WALL", 3, i, "i_tiles", gp));
        }
        for (int i = 0; i < 5; i++) {
            i_tiles.add(new UIEntity("WALL", 4, i, "i_tiles", gp));
        }

        i_tiles.add(new UIEntity("WATER", "i_tiles", gp));

        return i_tiles;
    }

    /**
     * DRAW
     * Draws the UI
     * Called by GamePanel
     * @param g2 Graphics2D engine
     */
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        drawGrid();

        if (gp.gameState == gp.editState) {
            drawEditState();
        }
        else if (gp.gameState == gp.playState) {
            drawPlayState();
        }
    }

    private void drawGrid() {
        // Semi-transparent white
        g2.setColor(new Color(255, 255, 255, 50));

        // Vertical lines
        for (int col = 0; col <= gp.maxWorldCol; col++) {
            int x = col * gp.tileSize;
            g2.drawLine(x, 0, x, gp.maxWorldRow * gp.tileSize);
        }

        // Horizontal lines
        for (int row = 0; row <= gp.maxWorldRow; row++) {
            int y = row * gp.tileSize;
            g2.drawLine(0, y, gp.maxWorldCol * gp.tileSize, y);
        }
    }

    private void drawEditState() {
        if (subState == 0) {
            // User holding down Y
            if (gp.keyH.yPressed) {
                drawEditing_Menu();
            }
            // User let go of Y, run once
            else if (wasYPressed) {
                editing_GetEntity();
            }
            // User not holding down y
            else {
                drawEditing_Map();
            }

            // Detect if Y is pressed
            wasYPressed = gp.keyH.yPressed;

            if (gp.keyH.startPressed) {
                gp.keyH.startPressed = false;
                subState = 1;
            }
        }
        else if (subState == 1) {
            drawEditing_Pause();
        }
        else if (subState == 2){
            drawEditing_SaveLoadDelete(true, false);
        }
        else if (subState == 3){
            drawEditing_SaveLoadDelete(false, true);
        }
        else if (subState == 4){
            drawEditing_SaveLoadDelete(false, false);
        }
    }

    private void drawEditing_Pause() {
        int frameX = gp.tileSize * 2;
        int frameY = gp.tileSize;

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));

        int textX = frameX + gp.tileSize * 2;
        int textY = frameY + (gp.tileSize * 2 + 10);

        // PLAY
        g2.drawString("Play Level", textX, textY);
        if (commandNum == 0) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyH.aPressed) {
                gp.keyH.aPressed = false;

                commandNum = 0;
                subState = 0;
                gp.gameState = gp.playState;
                gp.setupLevel();
            }
        }

        // SAVE
        textY += gp.tileSize;
        g2.drawString("Save Level", textX, textY);
        if (commandNum == 1) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyH.aPressed) {
                gp.keyH.aPressed = false;

                commandNum = 0;
                subState = 2;
            }
        }

        // LOAD
        textY += gp.tileSize;
        g2.drawString("Load Level", textX, textY);
        if (commandNum == 2) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyH.aPressed) {
                gp.keyH.aPressed = false;

                if (gp.saveFiles.isEmpty()) return;

                commandNum = 0;
                subState = 3;
            }
        }

        // DELETE
        textY += gp.tileSize;
        g2.drawString("Delete Level", textX, textY);
        if (commandNum == 3) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyH.aPressed) {
                gp.keyH.aPressed = false;

                if (gp.saveFiles.isEmpty()) return;

                commandNum = 0;
                subState = 4;
            }
        }

        if (gp.keyH.bPressed || gp.keyH.startPressed) {
            gp.keyH.bPressed = false;
            gp.keyH.startPressed = false;
            subState = 0;
            commandNum = 0;
        }

        if (gp.keyH.upPressed) {
            gp.keyH.upPressed = false;

            commandNum--;
            if (commandNum < 0) {
                commandNum = 0;
            }
        }
        else if (gp.keyH.downPressed) {
            gp.keyH.downPressed = false;

            commandNum++;
            if (commandNum > 3) {
                commandNum = 3;
            }
        }
    }
    private void drawEditing_SaveLoadDelete(boolean isSaving, boolean isLoading) {

        if (gp.saveFiles.isEmpty() && !isSaving) return;

        g2.setColor(Color.WHITE);

        int frameX = gp.tileSize * 2;
        int frameY = gp.tileSize;

        int textX = frameX + gp.tileSize * 2;
        int textY = frameY + (gp.tileSize * 2 + 10);
        String text;

        int i = 0;
        for (Map.Entry<String, String> entry : gp.saveFiles.entrySet()) {

            text = i + 1 + ")  " + entry.getValue();
            g2.drawString(text, textX, textY);

            if (commandNum == i) {
                g2.drawString(">", textX - 25, textY);

                if (gp.keyH.aPressed) {
                    gp.keyH.aPressed = false;

                    commandNum = 0;
                    subState = 0;

                    if (isSaving) {
                        gp.saveLoad.save(entry.getKey());
                    }
                    else if (isLoading) {
                        gp.saveLoad.load(entry.getKey());
                    }
                    else {
                        gp.saveLoad.delete(entry.getKey());
                    }
                }
            }

            i++;
            textY += gp.tileSize;
        }

        if (isSaving) {
            text = gp.saveFiles.size() + 1 + ")  NEW";
            g2.drawString(text, textX, textY);

            if (commandNum == gp.saveFiles.size()) {
                g2.drawString(">", textX - 25, textY);

                if (gp.keyH.aPressed) {
                    gp.keyH.aPressed = false;
                    gp.saveLoad.save("");
                    commandNum = 0;
                    subState = 1;
                }
            }
        }

        if (gp.keyH.bPressed || gp.keyH.startPressed) {
            gp.keyH.bPressed = false;
            gp.keyH.startPressed = false;
            commandNum = 0;
            subState = 1;
        }

        if (gp.keyH.upPressed) {
            gp.keyH.upPressed = false;

            commandNum--;
            if (commandNum < 0) {
                commandNum = 0;
            }
        }
        else if (gp.keyH.downPressed) {
            gp.keyH.downPressed = false;

            commandNum++;

            int maxSize = isSaving ? gp.saveFiles.size() : gp.saveFiles.size() - 1;
            if (commandNum > maxSize) {
                commandNum = maxSize;
            }
        }
    }

    private void drawEditing_Menu() {
        editing_menu();
        editing_menu_Input_Dir();
    }
    private void editing_menu() {

        int cursorX = (gp.screenWidth / 2) - gp.tileSize;
        int cursorY = (gp.screenHeight / 2) - gp.tileSize;

        int scrollOffsetX = cursorX - (entityListIndex * gp.tileSize);
        int scrollOffsetY = cursorY - (entityIndex * gp.tileSize);

        int x, y;
        for (int i = 0; i < entityLibrary.size(); i++) {

            // Shift lists left or right while keeping cursor the same
            x = scrollOffsetX + (i * gp.tileSize);

            // Shift entire list down to cursor's Y if current list
            y = entityListIndex == i ? scrollOffsetY : cursorY;

            for (int c = 0; c < entityLibrary.get(i).size(); c++) {

                // Reduce transparency if not active list
                if (i != entityListIndex) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                }
                g2.drawImage(entityLibrary.get(i).get(c).getImage(), x, y, gp.tileSize, gp.tileSize, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                // Draw cursor if current selection
                if (entityListIndex == i && entityIndex == c) {
                    g2.drawImage(cursor, cursorX, cursorY, gp.tileSize, gp.tileSize, null);
                }

                y += gp.tileSize;
            }
        }
    }
    private void editing_menu_Input_Dir() {
        if (gp.keyH.upPressed) {
            gp.keyH.upPressed = false;

            entityIndex--;
            if (entityIndex < 0) {
                entityIndex = entityLibrary.get(entityListIndex).size() - 1;
            }
        }
        else if (gp.keyH.downPressed) {
            gp.keyH.downPressed = false;

            entityIndex++;
            if (entityIndex > entityLibrary.get(entityListIndex).size() - 1) {
                entityIndex = 0;
            }
        }
        else if (gp.keyH.leftPressed) {
            gp.keyH.leftPressed = false;

            entityListIndex--;
            entityIndex = 0;
            if (entityListIndex < 0) {
                entityListIndex = entityLibrary.size() - 1;
            }
        }
        else if (gp.keyH.rightPressed) {
            gp.keyH.rightPressed = false;

            entityListIndex++;
            entityIndex = 0;
            if (entityListIndex > entityLibrary.size() - 1) {
                entityListIndex = 0;
            }
        }
    }

    public void editing_GetEntity() {
        UIEntity uiEntity = entityLibrary.get(entityListIndex).get(entityIndex);

        currentEntity = uiEntity.isWall() ?
                gp.eGenerator.getWall(uiEntity.getOri(), uiEntity.getSide()) :
                gp.eGenerator.getEntity(uiEntity.getName());

        currentEntity.worldX = slotCol;
        currentEntity.worldY = slotRow;

        currentEntityList = gp.getEntityList(entityListIndex);
    }

    private void drawEditing_Map() {
        editing_Map_Cursor();
        editing_Map_HUD();

        editing_Map_Input_A();
        editing_Map_Input_B();
        editing_Map_Input_Dir();
    }
    private void editing_Map_HUD() {
        if (selectedEntity == null) {
            drawCurrentEntity();
        }
    }
    private void drawCurrentEntity() {
        UIEntity uiEntity = entityLibrary.get(entityListIndex).get(entityIndex);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.drawImage(uiEntity.getImage(), slotCol + 7, slotRow + 7, gp.tileSize - 14, gp.tileSize - 14, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
    private void editing_Map_Cursor() {

        // Entity currently selected, draw sprite under cursor
        if (selectedEntity != null) {
            g2.drawImage(selectedEntity.image, slotCol, slotRow, gp.tileSize, gp.tileSize, null);
            g2.drawImage(cursor_select, slotCol, slotRow, gp.tileSize, gp.tileSize, null);
        }
        else {
            g2.drawImage(cursor, slotCol, slotRow, gp.tileSize, gp.tileSize, null);
        }
    }

    private void editing_Map_Input_A() {
        if (gp.keyH.aPressed) {
            gp.keyH.aPressed = false;

            // Cursor on existing entity and grabbed, return
            if (editing_GrabEntity()) return;

            // Entity currently grabbed, place down
            if (selectedEntity != null) {
                editing_PlaceEntity(selectedEntity, selectedEntityList);
                selectedEntity = null;
                selectedEntityList = null;
            }
            // Not currently holding entity, place down new one
            else {
                editing_GetEntity();
                editing_PlaceEntity(currentEntity, currentEntityList);
                currentEntity = null;
            }
        }
    }
    private void editing_Map_Input_B() {
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
    private void editing_Map_Input_Dir() {
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

    private boolean editing_GrabEntity() {
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
    private void editing_PlaceEntity(Entity entity, Entity[] entities) {
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

    private void drawPlayState() {
        playing_HUD();
    }

    /**
     * DRAW HUD
     * Draws the HUD during play state
     * called by draw()
     */
    private void playing_HUD() {
        playing_Debug();
    }

    /**
     * DRAW DEBUG
     * UI for debug information
     * Called by drawHUD()
     */
    private void playing_Debug() {

        if (gp.chr[0] == null) return;

        int x = 10;
        int y = gp.tileSize * 6;
        int lineHeight = 20;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));

        // Draw coordinates
        g2.drawString("WorldX: " + gp.chr[0].worldX, x, y);
        y += lineHeight;
        g2.drawString("WorldY: " + gp.chr[0].worldY, x, y);
        y += lineHeight;
        g2.drawString("Column: " + gp.chr[0].worldX / gp.tileSize, x, y);
        y += lineHeight;
        g2.drawString("Row: " + gp.chr[0].worldY / gp.tileSize, x, y);
    }

    private BufferedImage setup(String imagePath) {

        BufferedImage image = null;

        try {
            image = ImageIO.read(Objects.requireNonNull(
                    getClass().getResource(imagePath + ".png")
            ));
            image = GamePanel.utility.scaleImage(image, gp.tileSize, gp.tileSize);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return image;
    }
}
