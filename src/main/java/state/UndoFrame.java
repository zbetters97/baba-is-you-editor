package state;

import java.util.HashMap;

public record UndoFrame(HashMap<Integer, State> entities) {}

