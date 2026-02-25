package entity.tile_interactive;

import application.GamePanel;
import tile.InteractiveTile;

public class IT_Water extends InteractiveTile {

    public static final String iName = "WATER";

    public IT_Water(GamePanel gp, int x, int y) {
        super(gp);

        name = iName;

        worldX = x * gp.tileSize;
        worldY = y * gp.tileSize;

        image = up1 = down1 = left1 = right1 = setupImage("/interactive_tiles/it_" + iName.toLowerCase());
    }
}