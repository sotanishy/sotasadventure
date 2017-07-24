package sotasadventure;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This is a simple side-scrolling game.
 * This class contains the main method of this game.
 *
 * @author  Sota Nishiyama
 */
public class SotasAdventure {

    /**
     * Main method which starts the game.
     * This method creates a JFrame and a JPanel for this game.
     * The layout of the JPanel is CardLayout in order to change its contents.
     *
     * This method creates a StateMachine and add several States used in this game to it.
     *
     * This method sets a timer to update and render the contents every 100 miliseconds.
     *
     * @param args unused
     */
    public static void main (String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                // set up the JFrame and the JPanel
                JFrame frame = new JFrame();
                frame.setLayout(new BorderLayout());
                frame.setTitle("Sota's Adventure");
                frame.setIconImage(new ImageIcon(getClass().getResource("/resources/images/sota.png")).getImage());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setMinimumSize(new Dimension(500, 500));

                JPanel panel = new JPanel();
                panel.setPreferredSize(new Dimension(1200, 600));
                panel.setLayout(new CardLayout());

                frame.add(panel, BorderLayout.CENTER);
                frame.pack();
                frame.setLocationRelativeTo(null);

                // add States to the StateMachine
                StateMachine gameMode = new StateMachine(panel);
                gameMode.add("mainmenu", new MainMenuState(gameMode));
                gameMode.add("worldmap", new WorldMapState(gameMode));
                gameMode.add("stage", new StageState(gameMode));

                gameMode.change("mainmenu");

                frame.setVisible(true);

                // set and start the timer
                double start = new Date().getTime();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // calculate the elapsed time
                        double now = new Date().getTime();
                        double elapsedTime = (now - start) / 1000;

                        // update and render the current State
                        gameMode.update(elapsedTime);
                        gameMode.render();
                    }
                }, 0, 100);
            }
        });
    }
}
