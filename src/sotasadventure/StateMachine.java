package sotasadventure;

import java.awt.CardLayout;
import java.util.HashMap;

import javax.swing.JPanel;

/**
 * The class that stores and handles all states used in the game.
 * When updating and rendering contents in the game, methods in this class will be called instead of methods in each State by the game loop.
 * The class sets the current state to currentStage field and operates it through other methods.
 *
 * @author Sota Nishiyama
 */
public class StateMachine {

    private HashMap<String, State> states = new HashMap<String, State>();
    private State currentState = new EmptyState();
    private JPanel panel;

    private class EmptyState extends State {
        @Override
        public void update(double elapsedTime) {}

        @Override
        public void render() {}

        @Override
        public void enter(String optional) {}

        @Override
        public void exit() {}
    }

    /**
     * Sets the panel this state machine is applied to.
     * @param panel the panel this state machine is applied to
     */
    public StateMachine(JPanel panel) {
        this.panel = panel;
        panel.add(currentState);
    }

    /**
     * Calls the update method of the current state.
     * @param elapsedTime the time elapsed since the game started
     */
    public void update(double elapsedTime) {
        currentState.update(elapsedTime);
    }

    /**
     * Calls the render method of the current state.
     */
    public void render() {
        currentState.render();
    }

    /**
     * Changes the current state and give an optional string to the next state.
     * @param name the name of the state which comes next
     * @param opt the optional variable
     */
    public void change(String name, String opt) {
        // end the current state
        currentState.exit();

        // start the new state
        currentState = states.get(name);
        ((CardLayout) panel.getLayout()).show(panel, name);
        currentState.enter(opt);
    }

    /**
     * Changes the current state and give an empty string to the next state.
     * @param name the name of the state which comes next
     */
    public void change(String name) {
        change(name, "");
    }

    /**
     * Adds a state to the state machine.
     * @param name the name of the state to be added
     * @param state the state to be added
     */
    public void add(String name, State state) {
        states.put(name, state);
        panel.add(state, name);
    }
}
