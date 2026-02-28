package data;

import java.io.Serial;
import java.io.Serializable;

public class DataStorage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // FILE INFO
    String file_date;

    // ENTITY DATA ARRAYS
    String[][] names;
    int[][] worldX, worldY;
    int[][] ori, side;

    public String toString() {
        return file_date;
    }
}