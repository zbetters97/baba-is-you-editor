package application;

import javax.swing.*;
import java.util.Objects;

public class Driver {

    protected static JFrame window;

    /**
     * MAIN
     * The main method of the application
     */
    static void main() {

        window = new JFrame();

        // Window properties
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Baba is You");
        new Driver().setGameIcon();

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        // Load settings
        if (gamePanel.fullScreenOn) {
            window.setUndecorated(true);
        }

        // Resize window
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // Start game thread
        gamePanel.setupGame();
        gamePanel.startGameThread();
    }

    /**
     * SET ICON
     * Sets the game icon to triforce.png
     * Called by main()
     */
    private void setGameIcon() {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(
                getClass().getClassLoader().getResource("misc/baba_icon.png")));
        window.setIconImage(icon.getImage());
    }
}