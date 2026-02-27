package entity;

import application.GamePanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class UIEntity {

    private final String name;
    private boolean isWall = false;
    private int ori;
    private int side;
    private BufferedImage image;

    public UIEntity(String name, String path, GamePanel gp) {
        this.name = name;
        setupImage("/" + path + "/" + name.toLowerCase(), gp);
    }

    public UIEntity(String name, int ori, int side, String path, GamePanel gp) {
        this.name = name;
        this.ori = ori;
        this.side = side;
        isWall = true;
        setupImage("/" + path + "/" + name + "_" + ori + "_" + side, gp);
    }

    /**
     * SETUP IMAGE
     * @param imagePath Path to image file
     */
    private void setupImage(String imagePath, GamePanel gp) {
        try {
            image = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream(imagePath + ".png")
            ));

            image = GamePanel.utility.scaleImage(image, gp.tileSize, gp.tileSize);
        }
        catch (IOException e) {
            System.out.println("Error loading image:" + e.getMessage());
        }
    }

    public String getName() {
        return name;
    }
    public boolean isWall() {
        return isWall;
    }
    public int getOri() {
        return ori;
    }
    public int getSide() {
        return side;
    }
    public BufferedImage getImage() {
        return image;
    }
}
