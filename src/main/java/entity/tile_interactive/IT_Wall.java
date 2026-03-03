package entity.tile_interactive;

import application.GamePanel;
import entity.ITileEntity;

public class IT_Wall extends ITileEntity {
    public static final String iName = "WALL";

    public IT_Wall(GamePanel gp, int col, int row, int ori, int side) {
        super(gp, col, row, iName, ori, side);
    }
}
