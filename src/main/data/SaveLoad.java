package data;

import application.GamePanel;
import entity.Entity;

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

            // SAVE DATA TO DS
            DataStorage ds = new DataStorage();

            // CURRENT DATE/TIME
            ds.file_date = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date(System.currentTimeMillis()));

            ds.names = new String[4][][];
            ds.names[0] = new String[gp.maxLvls][gp.words[0].length];
            ds.names[1] = new String[gp.maxLvls][gp.iTiles[0].length];
            ds.names[2] = new String[gp.maxLvls][gp.obj[0].length];
            ds.names[3] = new String[gp.maxLvls][gp.chr[0].length];

            ds.worldX = new int[4][][];
            ds.worldX[0] = new int[gp.maxLvls][gp.words[0].length];
            ds.worldX[1] = new int[gp.maxLvls][gp.iTiles[0].length];
            ds.worldX[2] = new int[gp.maxLvls][gp.obj[0].length];
            ds.worldX[3] = new int[gp.maxLvls][gp.chr[0].length];

            ds.worldY = new int[4][][];
            ds.worldY[0] = new int[gp.maxLvls][gp.words[0].length];
            ds.worldY[1] = new int[gp.maxLvls][gp.iTiles[0].length];
            ds.worldY[2] = new int[gp.maxLvls][gp.obj[0].length];
            ds.worldY[3] = new int[gp.maxLvls][gp.chr[0].length];

            Entity[][][] entityLists = { gp.words, gp.iTiles, gp.obj, gp.chr };
            for (int type = 0; type < entityLists.length; type++) {

                Entity[][] entitiesByLevel =  entityLists[type];

                for (int level = 0; level < gp.maxLvls; level++) {

                    for (int i = 0; i < entitiesByLevel[level].length; i++) {

                        Entity e =  entitiesByLevel[level][i];
                        if (e == null) {
                            ds.names[type][level][i] = "NULL";
                        }
                        else {
                            ds.names[type][level][i] = e.name;
                            ds.worldX[type][level][i] = e.worldX;
                            ds.worldY[type][level][i] = e.worldY;
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

            Entity[][][] entityLists = { gp.words, gp.iTiles, gp.obj, gp.chr };
            for (int type = 0; type < entityLists.length; type++) {

                Entity[][] entitiesByLevel =  entityLists[type];

                for (int level = 0; level < gp.maxLvls; level++) {

                    for (int i = 0; i < entitiesByLevel[level].length; i++) {

                        String name = ds.names[type][level][i];

                        if ("NULL".equals(name)) {
                            entityLists[type][level][i] = null;
                            continue;
                        }

                        boolean shouldCreate = reload || entityLists[type][level][i] == null;

                        if (shouldCreate) {
                            Entity e = gp.eGenerator.getEntity(name);
                            e.worldX = ds.worldX[type][level][i];
                            e.worldY = ds.worldY[type][level][i];
                            entityLists[type][level][i] = e;
                        }
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