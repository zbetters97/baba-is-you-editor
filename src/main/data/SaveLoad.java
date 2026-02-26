package data;

import application.GamePanel;
import entity.Entity;
import entity.tile_interactive.IT_Wall;

import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class SaveLoad {

    public String[] saveFiles = {
            File.separator + "save_1.dat",
            File.separator + "save_2.dat",
            File.separator + "save_3.dat",
    };

    private final GamePanel gp;

    public SaveLoad(GamePanel gp) {
        this.gp = gp;
    }

    public void save(int saveSlot) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(gp.saveDir + saveFiles[saveSlot]));

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
            Entity[][] entityLists = { gp.words, gp.iTiles, gp.obj, gp.chr };
            for (int type = 0; type < entityLists.length; type++) {

                // Grab each entity array
                Entity[] entities =  entityLists[type];

                for (int i = 0; i < entities.length; i++) {

                    Entity e =  entities[i];
                    if (e == null) {
                        ds.names[type][i] = "NULL";

                        if (type == 1) {
                            ds.ori[i] = -1;
                            ds.side[i] = -1;
                        }
                    }
                    else {
                        ds.names[type][i] = e.name;
                        ds.worldX[type][i] = e.worldX;
                        ds.worldY[type][i] = e.worldY;

                        if (type == 1 && e instanceof IT_Wall) {
                            ds.ori[i] = e.ori;
                            ds.side[i] = e.side;
                        }
                        else {
                            ds.ori[i] = -1;
                            ds.side[i] = -1;
                        }                        
                    }
                }
            }

            // WRITE THE DS OBJECT
            oos.writeObject(ds);
            oos.close();
        }
        catch (Exception e) {
            System.out.println("SAVE ERROR! " + e.getMessage());
        }
    }

    public void load(int saveSlot, boolean reload) {

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(gp.saveDir + saveFiles[saveSlot]));

            // LOAD DATA FROM DS
            DataStorage ds = (DataStorage) ois.readObject();

            Entity[][] entityLists = { gp.words, gp.iTiles, gp.obj, gp.chr };
            for (int type = 0; type < entityLists.length; type++) {

                Entity[] entities =  entityLists[type];

                for (int i = 0; i < entities.length; i++) {

                    String name = ds.names[type][i];

                    if ("NULL".equals(name)) {
                        entities[i] = null;
                        continue;
                    }

                    boolean shouldCreate = reload || entities[i] == null;

                    if (shouldCreate) {
                        Entity e = (type == 1 && name.equals(IT_Wall.iName)) ?
                                gp.eGenerator.getWall(ds.ori[i], ds.side[i]) :
                                gp.eGenerator.getEntity(name);

                        e.worldX = ds.worldX[type][i];
                        e.worldY = ds.worldY[type][i];

                        entities[i] = e;
                    }
                }

            }

            ois.close();
        }
        catch (Exception e) {
            System.out.println("LOAD ERROR! " +e.getMessage());
        }
    }
}