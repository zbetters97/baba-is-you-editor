package tile;

import application.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class TileManager {

    private final GamePanel gp;
    public Tile[] tiles;

    /* [LEVEL NUMBER][ROW][COL] */
    public final int[][][] lvlTileNum;

    /**
     * CONSTRUCTOR
     * @param gp GamePanel
     */
    public TileManager(GamePanel gp) {
        this.gp = gp;
        lvlTileNum = new int[gp.maxLvls][33][18];
        loadTileData();
    }

    /**
     * LOAD LEVEL
     * Loads current level data
     */
    public void loadLvl() {

        // Import current level
        InputStream inputStream = getClass().getResourceAsStream("/levels/" + gp.lvlFiles[gp.currentLvl]);

        try {
            Scanner sc = new Scanner(Objects.requireNonNull(inputStream));

            for (int row = 0; sc.hasNextLine(); row++) {
                String line = sc.nextLine();
                String[] numbers = line.split(" ");

                for (int col = 0; col < numbers.length; col++) {
                    int tileNum = Integer.parseInt(numbers[col]);
                    lvlTileNum[gp.currentLvl][col][row] = tileNum;
                }
            }

            sc.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * LOAD TILE DATA
     * Loads the tile data from a text document
     */
    private void loadTileData() {
        // Arrays to hold tile attributes
        ArrayList<String> tileNumbers = new ArrayList<>();
        ArrayList<String> collisionStatus = new ArrayList<>();

        // Import tile data
        InputStream is = getClass().getResourceAsStream("/levels/lvl_tile_data.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)));

        // Add tile data to arrays
        try {
            String line;
            while ((line = br.readLine()) != null) {
                tileNumbers.add(line);
                collisionStatus.add(br.readLine());
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // Assign tiles array
        tiles = new Tile[tileNumbers.size()];

        String tileNumber;
        boolean hasCollision;

        // Loop through all tile data in fileNames
        for (int i = 0; i < tileNumbers.size(); i++) {

            // Assign each name to fileName
            tileNumber = tileNumbers.get(i);
            hasCollision = collisionStatus.get(i).equals("true");

            createTile(i, tileNumber, hasCollision);
        }
    }

    /**
     * CREATE TILE
     * Assigns tile attributes to the tiles array
     * @param index Array index
     * @param hasCollision True if tile has collision
     * @param tileNumber Tile number
     */
    private void createTile(int index, String tileNumber, boolean hasCollision) {
        try {
            tiles[index] = new Tile();

            tiles[index].image = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/tiles/" + tileNumber)
            ));

            tiles[index].image = GamePanel.utility.scaleImage(tiles[index].image, gp.tileSize, gp.tileSize);
            tiles[index].hasCollision = hasCollision;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * DRAW
     * Draws all the tiles to the screen
     * Called by GamePanel
     * @param g2 Graphics object
     */
    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;
        int x = 0;
        int y = 0;

        // Loop until column and row are filled
        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

            // Grab tile
            int tileNum = lvlTileNum[gp.currentLvl][worldCol][worldRow];

            // Draw to x/y
            g2.drawImage(tiles[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);

            // Proceed to next column
            worldCol++;
            x += gp.tileSize;

            // Reached last column, jump to next row and first column
            if (worldCol == gp.maxScreenCol) {
                worldCol = 0;
                worldRow++;
                x = 0;
                y += gp.tileSize;
            }
        }

        if (gp.showGrid) {
            drawGrid(g2);
        }
    }

    /**
     * DRAW GRID
     * Draws a gray grid to the screen
     * Called by draw()
     * @param g2 Graphics object
     */
    private void drawGrid(Graphics2D g2) {
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
}
