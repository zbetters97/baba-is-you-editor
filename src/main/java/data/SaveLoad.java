package data;

import application.GamePanel;
import entity.Entity;
import entity.tile_interactive.IT_Belt;
import entity.tile_interactive.IT_Wall;
import entity.tile_interactive.IT_Water;

import java.io.*;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.UUID;

public record SaveLoad(GamePanel gp) {

    public void resetData() {
        gp.entities.clear();
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
            int size = gp.entities.size();

            ds.names = new String[size];
            ds.worldX = new int[size];
            ds.worldY = new int[size];

            // Lists to store wall/water type values
            ds.belt_ori = new int[size];
            ds.wall_ori = new int[size];
            ds.wall_side = new int[size];
            ds.water_ori = new int[size];
            ds.water_side = new int[size];

            // Parse over each entity
            for (int i = 0; i < size; i++) {

                Entity e = gp.entities.get(i);

                // Entity not present
                if (e == null) {
                    ds.names[i] = "NULL";
                    ds.wall_ori[i] = -1;
                    ds.wall_side[i] = -1;

                    continue;
                }

                // Entity found, save data
                ds.names[i] = e.getName();
                ds.worldX[i] = e.getWorldX();
                ds.worldY[i] = e.getWorldY();

                // Entity is an iTile, save variance
                switch (e) {
                    case IT_Belt _ -> ds.belt_ori[i] = e.getOri();
                    case IT_Wall _ -> {
                        ds.wall_ori[i] = e.getOri();
                        ds.wall_side[i] = e.getSide();
                    }
                    case IT_Water _ -> {
                        ds.water_ori[i] = e.getOri();
                        ds.water_side[i] = e.getSide();
                    }
                    default -> {
                        ds.belt_ori[i] = -1;
                        ds.wall_ori[i] = -1;
                        ds.water_ori[i] = -1;
                        ds.wall_side[i] = -1;
                        ds.water_side[i] = -1;
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

            // Parse over each entity
            ArrayList<Entity> entities = gp.entities;
            entities.clear();

            for (int i = 0; i < ds.names.length; i++) {

                // Grab saved name from file
                String name = ds.names[i];

                // No data, skip
                if ("NULL".equals(name)) {
                    continue;
                }

                // Get type if iTile
                Entity e = switch (name) {
                    case (IT_Belt.iName) -> gp.eGenerator.getEntity(name, ds.belt_ori[i], 0);
                    case IT_Wall.iName -> gp.eGenerator.getEntity(name, ds.wall_ori[i], ds.wall_side[i]);
                    case IT_Water.iName -> gp.eGenerator.getEntity(name, ds.water_ori[i], ds.water_side[i]);
                    default -> gp.eGenerator.getEntity(name, -1, -1);
                };

                if (e == null) continue;

                e.setWorldX(ds.worldX[i]);
                e.setWorldY(ds.worldY[i]);

                // Assign to GamePanel entity list
                entities.add(e);
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