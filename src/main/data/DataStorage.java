package data;

import java.io.Serializable;

public class DataStorage implements Serializable {

    // FILE INFO
    String file_date;

    String[][][] names;
    int[][][] worldX, worldY;

    public String toString() {
        return file_date;
    }
}