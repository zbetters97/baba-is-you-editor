package data;

import application.GamePanel;
import entity.Entity;
import entity.ITileEntity;
import entity.tile_interactive.IT_Wall;
import entity.tile_interactive.IT_Water;

import java.io.*;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.UUID;

public record SaveLoad(GamePanel gp) {

    public void resetData() {
        for (Entity[] entities : new Entity[][]{gp.words, gp.iTiles, gp.obj, gp.chr}) {
            Arrays.fill(entities, null);
        }
    }

    public void save(String levelName, String fileName) {
        saveToData(levelName);
        saveToFile(fileName);
    }
    public void saveToData(String levelName) {
        try {
            DataStorage ds = new DataStorage();

            ds.level_name = levelName;

            // 01/31/2026 format
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            ds.file_date = sdf.format(new Date(System.currentTimeMillis()));

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

            // Lists to store wall/water type values
            ds.wall_ori = new int[4][gp.iTiles.length];
            ds.wall_side = new int[4][gp.iTiles.length];
            ds.water_ori = new int[4][gp.iTiles.length];
            ds.water_side = new int[4][gp.iTiles.length];

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
                            ds.wall_ori[type][i] = -1;
                            ds.wall_side[type][i] = -1;
                        }

                        continue;
                    }

                    // Entity found, save data
                    ds.names[type][i] = e.name;
                    ds.worldX[type][i] = e.worldX;
                    ds.worldY[type][i] = e.worldY;

                    // Entity is a wall, save variance
                    if (e instanceof ITileEntity) {
                        if (e instanceof IT_Wall) {
                            ds.wall_ori[type][i] = e.ori;
                            ds.wall_side[type][i] = e.side;
                        }
                        else if (e instanceof IT_Water) {
                            ds.water_ori[type][i] = e.ori;
                            ds.water_side[type][i] = e.side;
                        }
                    }
                    else {
                        ds.wall_ori[type][i] = -1;
                        ds.wall_side[type][i] = -1;
                    }
                }
            }

            gp.levelProgress = ds;
        }
        catch (Exception e) {
            System.out.println("Error saving level: " + e.getMessage());
        }
    }
    private void saveToFile(String fileName) {
        try {
            if (!gp.dbConnected || gp.levelProgress == null) return;

            // Create new save or overwrite existing one
            String fileID = fileName.isEmpty() ? UUID.randomUUID() + ".dat" : fileName;

            Path tempFile = Path.of(fileID);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile.toFile()));

            // Write to the DS object
            oos.writeObject(gp.levelProgress);
            oos.close();

            // Upload to Firebase storage
            gp.db.uploadLevel(tempFile);

            // Update stored save files
            gp.saveFiles = gp.db.getSaveFileNames();
        }
        catch (Exception e) {
            System.out.println("Error saving level: " + e.getMessage());
        }
    }

    public void load(String fileName) {
        loadFile(fileName);
        loadFromData();
    }
    private void loadFile(String fileName) {
        try {
            if (!gp.dbConnected) return;

            // Get save file from Firebase storage
            byte[] data = gp.db.downloadLevel(fileName);
            if (data == null) return;

            // Read saved file
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));

            // Load data to the DS object
            gp.levelProgress = (DataStorage) ois.readObject();

            // Close file
            ois.close();
        }
        catch (Exception e) {
            System.out.println("Error loading level: " + e.getMessage());
        }
    }
    public void loadFromData() {
        try {
            // Load data to the DS object
            DataStorage ds = gp.levelProgress;

            // Parse over each entity type
            Entity[][] entityLists = {gp.words, gp.iTiles, gp.obj, gp.chr};
            for (int type = 0; type < entityLists.length; type++) {

                // Parse over each entity
                Entity[] entities = entityLists[type];
                for (int i = 0; i < entities.length; i++) {

                    entities[i] = null;

                    // Grab saved name from file
                    String name = ds.names[type][i];

                    // No data, skip
                    if ("NULL".equals(name)) {
                        entities[i] = null;
                        continue;
                    }

                    // Get wall/water type if Wall/Water
                    Entity e = name.equals(IT_Wall.iName) ?
                            gp.eGenerator.getITile(name, ds.wall_ori[type][i], ds.wall_side[type][i]) :
                            gp.eGenerator.getEntity(name, ds.water_ori[type][i], ds.water_side[type][i]);

                    if (e == null) continue;

                    e.worldX = ds.worldX[type][i];
                    e.worldY = ds.worldY[type][i];

                    // Assign to GamePanel entity list
                    entities[i] = e;
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error loading level: " + e.getMessage());
        }
    }

    public void delete(String fileName) {
        if (!gp.dbConnected) return;

        if (gp.db.deleteLevel(fileName)) {
            gp.saveFiles = gp.db.getSaveFileNames();
        }
    }
}