package data;

import application.GamePanel;

import java.util.ArrayDeque;
import java.util.Deque;

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
        State[] entityStateStack = saveEntityStates();

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
     * @return Array of entity states
     */
    private State[] saveEntityStates() {

        // Array same size as GamePanel entity list
        State[] eStates = new State[gp.entities.length];

        for (int i = 0; i < gp.entities.length; i++) {
            if (gp.entities[i] == null) continue;

            // For each entity, create a new state at same index to hold current state
            eStates[i] = new State(
                    gp.entities[i].getName(),
                    gp.entities[i].getWorldX(),
                    gp.entities[i].getWorldY(),
                    gp.entities[i].getDirection(),
                    gp.entities[i].getOri(),
                    gp.entities[i].getSide()

            );
        }

        return eStates;
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

    /**
     * LOAD ENTITY STATES
     * Iterates over the given stack and loads the
     *  data inside to the entities found in the given list
     * Called by loadState()
     * @param saved The stack to load the entity data from
     */
    private void loadEntityStates(State[] saved) {

        // Iterate over each entity in given list
        for (int i = 0; i < gp.entities.length; i++) {

            // No data, entity is null, go to next
            if (saved[i] == null) {
                gp.entities[i] = null;
                continue;
            }

            boolean revived = false;

            // Has data but entity is now null
            if (gp.entities[i] == null) {

                // Resurrect entity using saved state
                gp.entities[i] = gp.eGenerator.getEntity(saved[i].name, saved[i].ori, saved[i].side);
                gp.entities[i].setAlive(true);
                revived = true;
            }
            // Entity changed since redo
            else if (!gp.entities[i].getName().equals(saved[i].name)) {
                gp.entities[i].transform(gp.eGenerator.getEntity(saved[i].name, saved[i].ori, saved[i].side));
            }

            if (gp.entities[i] == null) continue;

            // Assign values to entity
            gp.entities[i].setPreviousWorldX(saved[i].point.x);
            gp.entities[i].setPreviousWorldY(saved[i].point.y);
            gp.entities[i].setDirection(saved[i].direction);

            // Entity resurrected, place back at saved X/Y
            if (revived) {
                gp.entities[i].setWorldX(saved[i].point.x);
                gp.entities[i].setWorldY(saved[i].point.y);
            }
            else if (gp.entities[i].getWorldX() != saved[i].point.x || gp.entities[i].getWorldY() != saved[i].point.y) {
                gp.entities[i].setReversing(true);
            }
        }
    }

    public void clearData() {
        undoStack.clear();
    }
}
