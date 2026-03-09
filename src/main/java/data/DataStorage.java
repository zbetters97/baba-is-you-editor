package data;

import java.io.Serial;
import java.io.Serializable;

public class DataStorage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // FILE INFO
    String level_name;
    String file_date;

    // ENTITY DATA ARRAYS
    String[] names;
    int[] worldX, worldY;
    int[] belt_ori, wall_ori, water_ori;
    int[] wall_side, water_side;

    public String toString() {
        return level_name + " [" + file_date + "]";
    }
}