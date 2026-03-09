package entity.tile_interactive;

import application.GamePanel;
import entity.ITileEntity;

public class IT_Belt extends ITileEntity {
    public static final String iName = "BELT";

    public IT_Belt(GamePanel gp, int col, int row, int ori) {
        super(gp, col, row, iName, ori, 0);

        if (ori == 0) direction = GamePanel.Direction.UP;
        else if (ori == 1) direction = GamePanel.Direction.DOWN;
        else if (ori == 2) direction = GamePanel.Direction.LEFT;
        else direction = GamePanel.Direction.RIGHT;


    }
}
