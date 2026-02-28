package data;

import application.GamePanel;
import entity.Entity;
import entity.tile_interactive.IT_Wall;

import java.io.*;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;

public record SaveLoad(GamePanel gp) {

    public void save(String fileName) {
        try {
            if (!gp.dbConnected) return;

            boolean overwrite = !fileName.isEmpty();

            // Create new save or overwrite existing one
            String fileID = overwrite ? fileName : UUID.randomUUID() + ".dat";

            Path tempFile = Path.of(fileID);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile.toFile()));

            // Save data to DS object
            DataStorage ds = new DataStorage();

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy - hh:mm:ss a (z)");
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

            // Lists to store wall type values
            ds.ori = new int[4][gp.iTiles.length];
            ds.side = new int[4][gp.iTiles.length];

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
                            ds.ori[type][i] = -1;
                            ds.side[type][i] = -1;
                        }

                        continue;
                    }

                    // Entity found, save data
                    ds.names[type][i] = e.name;
                    ds.worldX[type][i] = e.worldX;
                    ds.worldY[type][i] = e.worldY;

                    // Entity is a wall, save variance
                    if (type == 1 && e instanceof IT_Wall) {
                        ds.ori[type][i] = e.ori;
                        ds.side[type][i] = e.side;
                    } else {
                        ds.ori[type][i] = -1;
                        ds.side[type][i] = -1;
                    }
                }
            }

            // Write to the DS object
            oos.writeObject(ds);
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
        try {
            if (!gp.dbConnected) return;

            // Get save file from Firebase storage
            byte[] data = gp.db.downloadLevel(fileName);
            if (data == null) return;

            // Read saved file
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));

            // Load data to the DS object
            DataStorage ds = (DataStorage) ois.readObject();

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

                    // Saved entity is a wall, get Wall entity...
                    // ...otherwise, get normal entity
                    boolean isWall = type == 1 && name.equals(IT_Wall.iName);
                    Entity e = isWall ?
                            gp.eGenerator.getWall(ds.ori[type][i], ds.side[type][i]) :
                            gp.eGenerator.getEntity(name);

                    e.worldX = ds.worldX[type][i];
                    e.worldY = ds.worldY[type][i];

                    // Assign to GamePanel entity list
                    entities[i] = e;
                }
            }

            // Close file
            ois.close();
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