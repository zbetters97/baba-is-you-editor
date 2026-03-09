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

        image = up1 = setupImage("/i_tiles/" + name.toLowerCase() + "_0_0");
        down1 = setupImage("/i_tiles/" + name.toLowerCase() + "_1_0");
        left1 = setupImage("/i_tiles/" + name.toLowerCase() + "_2_0");
        right1 = setupImage("/i_tiles/" + name.toLowerCase() + "_3_0");
    }
}
