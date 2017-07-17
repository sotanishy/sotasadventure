package sotasadventure;

import java.awt.CardLayout;
import java.util.HashMap;

import javax.swing.JPanel;

/**
 * The class that stores and handles all states used in the game.
 * When updating and rendering contents in the game, methods in this class will be called instead of methods in each State.
 * The class sets the current state to currentStage field and operates it through other methods.
 * 
 * @author Sota Nishiyama
 * @since  1.0
 */

public class StateMachine {

	private HashMap<String, State> states = new HashMap<String, State>();
	private State currentState = new State();
	private JPanel panel;

	/**
	 * Sets the panel this state machine is applied to.
	 * 
	 * @param panel the panel this state machine is applied to
	 */
	public StateMachine(JPanel panel) {
		this.panel = panel;
		panel.add(currentState);
	}

	/**
	 * Calls the update method of the current state.
	 * 
	 * @param elapsedTime the time elapsed since the game started
	 */
	public void update(float elapsedTime) {
		currentState.update(elapsedTime);
	}

	/**
	 * Calls the render method of the current state.
	 */
	public void render() {
		currentState.render();
	}

	/**
	 * Changes the current state.
	 * 
	 * @param name the name of the state which comes next
	 */
	public void change(String name) {
		// end the current state
		currentState.exit();

		// start the new state
		currentState = states.get(name);
		((CardLayout) panel.getLayout()).show(panel, name);
		currentState.enter();
	}

	/**
	 * Changes the current state and give a value to the next state.
	 * 
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
	 * Adds a state to the state machine.
	 * 
	 * @param name the name of the state to be added
	 * @param state the state to be added
	 */
	public void add(String name, State state) {
		states.put(name, state);
		panel.add(state, name);
	}
}