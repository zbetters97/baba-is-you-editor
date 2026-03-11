package entity;

import application.GamePanel;

import java.awt.*;

public abstract class ITileEntity extends Entity {

    public ITileEntity(GamePanel gp, int col, int row, String name, int ori, int side) {
        super(gp);

        this.point = new Point(col * gp.tileSize, row * gp.tileSize);
        setPreviousPoint(point);
        this.name = name;
        this.ori = ori;
        this.side = side;

        image = up1 = down1 = left1 = right1 = setupImage("/i_tiles/" + name.toLowerCase() + "_" + ori + "_" + side);
    }
}