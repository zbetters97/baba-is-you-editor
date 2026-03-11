package data;

import application.GamePanel;
import entity.Entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

public class StateHandler {

    private final GamePanel gp;

    // Max number of redo-s allowed
    private final static int MAX_UNDO = 75;

    // Stack to hold Object of entity states
    private final Deque<UndoFrame> undoStack = new ArrayDeque<>();

    /**
     * CONSTRUCTOR
     * @param pg GamePanel object
     */
    public StateHandler(GamePanel pg) {
        this.gp = pg;
    }

    /**
     * SAVE STATE
     * Saves entity states to each set of entities
     * Called by GamePanel when canSave is TRUE
     */
    public void saveState() {
        HashMap<Integer, State> entityStateStack = saveEntityStates();

        undoStack.push(new UndoFrame(entityStateStack));

        // Keep stack under max undo limit
        while (undoStack.size() > MAX_UNDO) {
            undoStack.removeLast();
        }
    }

    /**
     * SAVE ENTITY STATES
     * Iterates over the given entity array and saves the creates an
     *  array list inside the given stack
     * Called by saveState()
     * @return ArrayList of entity states
     */
    private HashMap<Integer, State> saveEntityStates() {

        HashMap<Integer, State> states = new HashMap<>();

        for (Entity e : gp.entities) {
            states.put(e.getId(), new State(
                    e.getName(),
                    e.getPoint(),
                    e.getPreviousPoint(),
                    e.getDirection(),
                    e.getOri(),
                    e.getSide()
            ));
        }

        return states;
    }

    /**
     * LOAD STATE
     * Loads entity states from the stacks and assigns
     *  the loaded values to each entity in each entity list
     * Called by GamePanel when B is pressed
     */
    public void loadState() {
        if (undoStack.isEmpty()) return;

        UndoFrame frame = undoStack.pop();
        loadEntityStates(frame.entities());
    }

    private void loadEntityStates(HashMap<Integer, State> saved) {

        for (Entity e : gp.entities) {

            State s = saved.get(e.getId());

            if (s == null) {
                e.setAlive(false);
                continue;
            }

            // Entity changed forms since last redo
            if (!e.getName().equals(s.name)) {
                e.transform(gp.eGenerator.getEntity(s.name, s.ori, s.side));
            }

            e.setPreviousPoint(s.point);
            e.setDirection(s.direction);

            // Entity placed on different tile, reverse to original tile
            if (!e.getPoint().equals(e.getPreviousPoint())) {
                e.setReversing(true);
            }
        }

        for (Integer id : saved.keySet()) {

            boolean found = false;

            for (Entity e : gp.entities) {
                if (e.getId() == id) {
                    found = true;
                    break;
                }
            }

            if (!found) {

                State s = saved.get(id);
                Entity e = gp.eGenerator.getEntity(s.name, s.ori, s.side);
                if (e == null) continue;

                e.setAlive(true);
                e.setId(id);

                e.setPoint(s.point);
                e.setDirection(s.direction);

                gp.entities.add(e);
            }
        }
    }

    public void clearData() {
        undoStack.clear();
    }
}
