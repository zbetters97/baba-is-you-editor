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

        String orientation;
        if (ori == 0) orientation = "hor";
        else if (ori == 1) orientation = "ver";
        else orientation = "cor";

        String facing;
        if (side == 0) facing = "left";
        else if (side == 1) facing = "mid";
        else if (side == 2) facing = "right";
        else if (side == 3) facing = "up";
        else if (side == 4) facing = "down";
        else if (side == 5) facing = "left-down";
        else if (side == 6) facing = "right-down";
        else if (side == 7) facing = "left-up";
        else facing = "right-up";

        image = up1 = down1 = left1 = right1 = setupImage("/interactive_tiles/it_" + iName.toLowerCase() + "_" + orientation + "_" + facing);
    }
}
