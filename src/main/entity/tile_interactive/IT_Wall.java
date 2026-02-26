package entity.tile_interactive;

import application.GamePanel;
import tile.InteractiveTile;

public class IT_Wall extends InteractiveTile {
    public static final String iName = "WALL";

    public IT_Wall(GamePanel gp, int x, int y, int ori, int side) {
        super(gp);

        name = iName;

        worldX = x * gp.tileSize;
        worldY = y * gp.tileSize;

        image = up1 = down1 = left1 = right1 = setupImage("/i_tiles/" + iName.toLowerCase() + "_" + ori + "_" + side);
    }
}
