package sotasadventure;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * The class that shows the world map.
 * @author Sota Nishiyama
 */
public class WorldMapState extends State {

    private StateMachine gameMode;

    private int lives;
    private int coins;
    private int gems;

    private class Stage {
        private String name;
        private boolean cleared = false;
        private boolean available = false;

        private Stage(String name) {
            this.name = name;
        }
    }

    private Stage[] stages = {
        new Stage("northAmerica"),
        new Stage("southAmerica"),
        new Stage("africa"),
        new Stage("europe"),
        new Stage("asia"),
        new Stage("oceania"),
        new Stage("antarctica")
    };

    private JButton[] stageButtons = new JButton[stages.length];

    private Image worldmap;
    private Image heartImage;
    private Image coinImage;
    private Image gemImage;

    private ImageIcon blue;
    private ImageIcon blueHover;
    private ImageIcon red;
    private ImageIcon redHover;
    private ImageIcon black;
    private ImageIcon blackHover;
    private ImageIcon helpImage;
    private ImageIcon helpImageHover;

    /**
     * Sets the background picture.
     * @param gameMode the state machine of the game
     */
    public WorldMapState(StateMachine gameMode) {
        this.gameMode = gameMode;

        loadResources();

        int row = 20;
        int col = 30;
        setLayout(new GridLayout(row, col));

        // stage buttons
        for (int i = 0; i < stages.length; i++) {
            stageButtons[i] = new JButton();
            stageButtons[i].setBorder(BorderFactory.createEmptyBorder());
            stageButtons[i].setContentAreaFilled(false);
            stageButtons[i].setRolloverEnabled(true);

            stageButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Stage stage = stages[getIndex((JButton) e.getSource())];
                    if (stage.available) {
                        gameMode.change("stage", stage.name);
                    }

                }
            });
        }

        JButton howToPlay = new JButton("How To Play");
        howToPlay.setBorder(BorderFactory.createEmptyBorder());
        howToPlay.setContentAreaFilled(false);
        howToPlay.setIcon(helpImage);
        howToPlay.setRolloverEnabled(true);
        howToPlay.setRolloverIcon(helpImageHover);
        howToPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                "How To Play Sota's Adventure\n" +
                "Press \u2190 \u2192 to move\n" +
                "Press \u2191 to jump/enter a door\n" +
                "Press A to attack by a sword\n" +
                "Press F to fire a gun");
            }
        });

        // add buttons
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (i == 2 && j == 1) add(howToPlay);
                else if (i == 4 && j == 7) add(stageButtons[0]);
                else if (i == 12 && j == 9) add(stageButtons[1]);
                else if (i == 10 && j == 16) add(stageButtons[2]);
                else if (i == 4 && j == 15) add(stageButtons[3]);
                else if (i == 5 && j == 22) add(stageButtons[4]);
                else if (i == 13 && j == 25) add(stageButtons[5]);
                else if (i == 18 && j == 15) add(stageButtons[6]);
                else add(new JLabel());
            }
        }
    }

    /**
     * Returns the index of the given JButton.
     * @param jb
     */
    private int getIndex(JButton jb) {
        for (int i = 0; i < stages.length; i++) {
            if (stageButtons[i] == jb) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void update(double elapsedTime) {}

    @Override
    public void render() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(Util.getScaledImage(worldmap, (int) getSize().getWidth(), (int) getSize().getHeight()), 0, 0, null);

        g.setFont(new Font("Consolas", Font.PLAIN, 30));
        g.setColor(Color.WHITE);

        g.drawImage(heartImage, 10, 5, null);
        g.drawString(" x " + lives, 50, 40);

        g.drawImage(coinImage, 150, 5, null);
        g.drawString(" x " + coins, 190, 40);

        g.drawImage(gemImage, 290, 5, null);
        g.drawString(" x " + gems, 330, 40);
    }

    @Override
    public void enter(String optional) {
        Preferences prefs = Preferences.userNodeForPackage(WorldMapState.class);
        lives = prefs.getInt("lives", 5);
        coins = prefs.getInt("coin", 0);
        gems = prefs.getInt("gem", 0);

        for (int i = 0; i < stages.length; i++) {
            stages[i].cleared = prefs.get(stages[i].name, "not cleared").equals("cleared");

            if (stages[i].cleared) {
                stages[i].available = true;
                stageButtons[i].setIcon(blue);
                stageButtons[i].setRolloverIcon(blueHover);
            } else {
                if (i == 0 || stages[i - 1].cleared) {
                    stages[i].available = true;
                    stageButtons[i].setIcon(red);
                    stageButtons[i].setRolloverIcon(redHover);
                } else {
                    stages[i].available = false;
                    stageButtons[i].setIcon(black);
                    stageButtons[i].setRolloverIcon(blackHover);
                }
            }
        }
    }

    @Override
    public void exit() {}

    /**
     * Loads images.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/world-map.png"));
        worldmap = ii.getImage();

        ii = new ImageIcon(getClass().getResource("/resources/images/heart.png"));
        heartImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/coin.png"));
        coinImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/gem.png"));
        gemImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/button-blue.png"));
        blue = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/button-blue-hover.png"));
        blueHover = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/button-red.png"));
        red = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/button-red-hover.png"));
        redHover = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/button-black.png"));
        black = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/button-black-hover.png"));
        blackHover = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/resources/images/help.png"));
        helpImage = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/resources/images/help-hover.png"));
        helpImageHover = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));
    }

}
