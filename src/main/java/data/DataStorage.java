package data;

import java.io.Serial;
import java.io.Serializable;

public class DataStorage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // FILE INFO
    public String level_name;
    public String file_date;
    public int song;

    // ENTITY DATA ARRAYS
    public String[] names;
    public int[] worldX, worldY;
    public int[] belt_ori, wall_ori, water_ori;
    public int[] wall_side, water_side;

    public String toString() {
        return level_name + " [" + file_date + "]";
    }
}