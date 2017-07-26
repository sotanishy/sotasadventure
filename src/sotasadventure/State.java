package sotasadventure;

import javax.swing.JPanel;

/**
 * The super class of states.
 * This class contains some methods that is used to update and render contents and help transitions.
 *
 * @author Sota Nishiyama
 */
public abstract class State extends JPanel {

    /**
     * Updates contents.
     * @param elapsedTime The time elapsed since the start of the game
     */
    public void update(double elapsedTime) {}

    /**
     * Render changes.
     */
    public void render() {}

    /**
     * Called at the beginning of the state.
     * @param optional the optional variable
     */
    public void enter(String optional) {}

    /**
     * Called at the end of the state.
     */
    public void exit() {}
}
