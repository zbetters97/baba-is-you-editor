package data;

import application.GamePanel;

import java.io.*;

public class ConfigManager {

    private final GamePanel gp;

    public ConfigManager(GamePanel gp) {
        this.gp = gp;
    }

    public void saveConfig() {

        try {
            // IMPORT FILE
            BufferedWriter bw = new BufferedWriter(new FileWriter(gp.saveDir + File.separator + "config.txt"));

            bw.write("USERNAME\n" + gp.username);
            bw.newLine();

            // FULLSCREEN
            bw.write("FULLSCREEN\n" + gp.fullScreenOn);
            bw.newLine();

            // MUSIC VOLUME
            bw.write("MUSIC VOLUME\n" + gp.music.volumeScale);
            bw.newLine();

            // SOUND EFFECTS VOLUME
            bw.write("SE VOLUME\n" + gp.se.volumeScale);
            bw.newLine();

            // CLOSE FILE
            bw.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadConfig() {

        try {
            // IMPORT FILE
            BufferedReader br = new BufferedReader(new FileReader(gp.saveDir + File.separator + "config.txt"));

            br.readLine();

            // USERNAME
            String s = br.readLine();
            gp.username = s;
            br.readLine();

            // FULL SCREEN
            s = br.readLine();
            gp.fullScreenOn = Boolean.parseBoolean(s);
            br.readLine();

            // MUSIC VOLUME
            s = br.readLine();
            gp.music.volumeScale = Integer.parseInt(s);
            br.readLine();

            // SOUND EFFECTS VOLUME
            s = br.readLine();
            gp.se.volumeScale = Integer.parseInt(s);
            br.readLine();

            br.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

            gp.username = "steelpro";
            gp.levelPath = "levels/" + gp.username + "/";


            // FULL SCREEN
            gp.fullScreenOn = false;

            // MUSIC VOLUME
            gp.music.volumeScale = 3;

            // SOUND EFFECTS VOLUME
            gp.se.volumeScale = 3;
        }
    }
}