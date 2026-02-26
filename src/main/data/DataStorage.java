package data;

import java.io.Serializable;

public class DataStorage implements Serializable {

    // FILE INFO
    String file_date;

    // ENTITY DATA ARRAYS
    String[][] names;
    int[][] worldX, worldY;
    int[] ori, side;

    public String toString() {
        return file_date;
    }
}