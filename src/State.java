package sotasadventure;

import javax.swing.JPanel;

/**
 * The super class of states.
 * This class contains some methods that is used to update contents and help transition.
 * 
 * @author Sota Nishiyama
 * @since  1.0
 */
public class State extends JPanel {

	/**
	 * Updates contents.
	 * 
	 * @param elapsedTime The time elapsed since the start of the game
	 */
	public void update(float elapsedTime) {}

	/**
	 * Render changes.
	 */
	public void render() {}

	/**
	 * Called at the beginning of the state.
	 */
	public void enter() {}

	/**
	 * Called at the beginning of the state.
	 * 
	 * @param opt the optional variable
	 */
	public void enter(String opt) {}

	/**
	 * Called at the end of the state.
	 */
	public void exit() {}
}