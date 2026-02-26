package data;

import application.GamePanel;
import entity.Entity;
import entity.tile_interactive.IT_Wall;

import java.io.*;
import java.nio.file.Files;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;

public record SaveLoad(GamePanel gp) {

    public void save(int saveSlot) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(gp.saveDir + "/save_" + saveSlot + ".dat"));

            // Save data to DS object
            DataStorage ds = new DataStorage();

            // Current data/time
            ds.file_date = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date(System.currentTimeMillis()));

            // Initialize entity data lists
            ds.names = new String[4][];
            ds.names[0] = new String[gp.words.length];
            ds.names[1] = new String[gp.iTiles.length];
            ds.names[2] = new String[gp.obj.length];
            ds.names[3] = new String[gp.chr.length];

            ds.worldX = new int[4][];
            ds.worldX[0] = new int[gp.words.length];
            ds.worldX[1] = new int[gp.iTiles.length];
            ds.worldX[2] = new int[gp.obj.length];
            ds.worldX[3] = new int[gp.chr.length];

            ds.worldY = new int[4][];
            ds.worldY[0] = new int[gp.words.length];
            ds.worldY[1] = new int[gp.iTiles.length];
            ds.worldY[2] = new int[gp.obj.length];
            ds.worldY[3] = new int[gp.chr.length];

            // Lists to store wall type values
            ds.ori = new int[gp.iTiles.length];
            ds.side = new int[gp.iTiles.length];

            // Parse over each entity type
            Entity[][] entityLists = {gp.words, gp.iTiles, gp.obj, gp.chr};
            for (int type = 0; type < entityLists.length; type++) {

                // Parse over each entity
                Entity[] entities = entityLists[type];
                for (int i = 0; i < entities.length; i++) {

                    Entity e = entities[i];

                    // Entity not present
                    if (e == null) {
                        ds.names[type][i] = "NULL";

                        if (type == 1) {
                            ds.ori[i] = -1;
                            ds.side[i] = -1;
                        }

                        continue;
                    }

                    // Entity found, save data
                    ds.names[type][i] = e.name;
                    ds.worldX[type][i] = e.worldX;
                    ds.worldY[type][i] = e.worldY;

                    // Entity is a wall, save variance
                    if (type == 1 && e instanceof IT_Wall) {
                        ds.ori[i] = e.ori;
                        ds.side[i] = e.side;
                    } else {
                        ds.ori[i] = -1;
                        ds.side[i] = -1;
                    }
                }
            }

            // Write to the DS object
            oos.writeObject(ds);
            oos.close();
        } catch (Exception e) {
            System.out.println("SAVE ERROR: " + e.getMessage());
        }
    }

    public void load(int saveSlot) {

        try {
            // Retrieve saved file
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(gp.saveDir + "/save_" + saveSlot + ".dat"));

            // Load data to the DS object
            DataStorage ds = (DataStorage) ois.readObject();

            // Parse over each entity type
            Entity[][] entityLists = {gp.words, gp.iTiles, gp.obj, gp.chr};
            for (int type = 0; type < entityLists.length; type++) {

                // Parse over each entity
                Entity[] entities = entityLists[type];
                for (int i = 0; i < entities.length; i++) {

                    // Grab saved name from file
                    String name = ds.names[type][i];

                    // No data, skip
                    if ("NULL".equals(name)) {
                        entities[i] = null;
                        continue;
                    }

                    // Saved entity is a wall, get Wall entity...
                    // ...otherwise, get normal entity
                    boolean isWall = type == 1 && name.equals(IT_Wall.iName);
                    Entity e = isWall ?
                            gp.eGenerator.getWall(ds.ori[i], ds.side[i]) :
                            gp.eGenerator.getEntity(name);

                    e.worldX = ds.worldX[type][i];
                    e.worldY = ds.worldY[type][i];

                    // Assign to GamePanel entity list
                    entities[i] = e;
                }
            }

            ois.close();
        } catch (Exception e) {
            System.out.println("LOAD ERROR: " + e.getMessage());
        }
    }

    public ArrayList<String> getSaveFiles() {

        ArrayList<String> fileNames = new ArrayList<>();

        try (var paths = Files.list(gp.saveDir.toPath())) {
            fileNames = paths
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.startsWith("save_") && name.endsWith(".dat"))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return fileNames;
    }

    public String getFileName(int saveSlot) {

        try {
            File saveFile = new File(gp.saveDir + "/save_" + saveSlot + ".dat");

            if (saveFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile));
                DataStorage ds = (DataStorage) ois.readObject();

                ois.close();
                return ds.toString();
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}