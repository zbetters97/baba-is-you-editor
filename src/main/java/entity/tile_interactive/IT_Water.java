package entity.tile_interactive;

import application.GamePanel;
import entity.ITileEntity;

public class IT_Water extends ITileEntity {
    public static final String iName = "WATER";

    public IT_Water(GamePanel gp, int col, int row, int ori, int side) {
        super(gp, col, row, iName, ori, side);
    }
}