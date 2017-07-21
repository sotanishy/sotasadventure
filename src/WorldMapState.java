package sotasadventure;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * The class that shows the world map.
 */

public class WorldMapState extends State {

    private StateMachine gameMode;

    private int lives;
    private int coins;
    private int gems;

    private String[][] stages = {
        {"sampleStage", "not cleared"},
        {"northAmerica", "not cleared"},
        {"southAmerica", "not cleared"},
        {"africa", "not cleared"},
        {"europe", "not cleared"},
        {"asia", "not cleared"},
        {"oceania", "not cleared"},
        {"antarctica", "not cleared"}
    };

    private JButton[] stageButtons = new JButton[stages.length];

    private Image worldmap;
    private Image heartImage;
    private Image coinImage;
    private Image gemImage;

    private ImageIcon[][] buttonImages = new ImageIcon[3][2];

    /**
     * Sets the background picture.
     * @param gameMode the state machine of the game
     */
    public WorldMapState(StateMachine gameMode) {
        this.gameMode = gameMode;

        loadImages();

        int row = 20;
        int col = 30;
        setLayout(new GridLayout(row, col));

        // stage labels and buttons
        for (int i = 0; i < stages.length; i++) {
            stageButtons[i] = new JButton();
            stageButtons[i].setBorder(BorderFactory.createEmptyBorder());
            stageButtons[i].setContentAreaFilled(false);

            stageButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int i = getIndex((JButton) e.getSource());
                    if (i == 0 || stages[i - 1][1].equals("cleared")) {
                        gameMode.change("stage", stages[i][0]);
                    }
                }
            });

            stageButtons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    JButton source = (JButton) e.getSource();
                    for (int i = 0; i < buttonImages.length; i++) {
                        if (source.getIcon() == buttonImages[i][0]) {
                            source.setIcon(buttonImages[i][1]);
                        }
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    JButton source = (JButton) e.getSource();
                    for (int i = 0; i < buttonImages.length; i++) {
                        if (source.getIcon() == buttonImages[i][1]) {
                            source.setIcon(buttonImages[i][0]);
                        }
                    }
                }
            });
        }

        // add buttons
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (i == 1 && j == 12) add(stageButtons[0]);
                else if (i == 4 && j == 7) add(stageButtons[1]);
                else if (i == 12 && j == 9) add(stageButtons[2]);
                else if (i == 10 && j == 16) add(stageButtons[3]);
                else if (i == 4 && j == 15) add(stageButtons[4]);
                else if (i == 5 && j == 22) add(stageButtons[5]);
                else if (i == 13 && j == 25) add(stageButtons[6]);
                else if (i == 18 && j == 15) add(stageButtons[7]);
                else add(new JLabel());
            }
        }
    }

    /**
     * Returns the index of the given JButton in stageButtons.
     */
    private int getIndex(JButton jb) {
        for (int i = 0; i < stageButtons.length; i++) {
            if (stageButtons[i] == jb) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void update(float elapsedTime) {}

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

        g.drawImage(gemImage, 150, 5, null);
        g.drawString(" x " + gems, 190, 40);

        g.drawImage(coinImage, 290, 5, null);
        g.drawString(" x " + coins, 330, 40);
    }

    @Override
    public void enter() {
        Properties prop = new Properties();
        InputStream input = null;
        OutputStream output = null;

        try {
            input = getClass().getResourceAsStream("/data.properties");
            prop.load(input);

            lives = Integer.parseInt(prop.getProperty("lives"));
            coins = Integer.parseInt(prop.getProperty("coin"));
            gems = Integer.parseInt(prop.getProperty("gem"));

            for (int i = 0; i < stages.length; i++) {
                stages[i][1] = prop.getProperty(stages[i][0]);

                if (stages[i][1].equals("cleared")) {
                    stageButtons[i].setIcon(buttonImages[0][0]);
                } else {
                    if (i == 0 || stages[i - 1][1].equals("cleared")) {
                        stageButtons[i].setIcon(buttonImages[1][0]);
                    } else {
                        stageButtons[i].setIcon(buttonImages[2][0]);
                    }
                }
            }

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {}

    /**
     * Loads images of the world map and its contents.
     */
    private void loadImages() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/images/world-map.png"));
        worldmap = ii.getImage();

        ii = new ImageIcon(getClass().getResource("/images/heart.png"));
        heartImage = Util.getScaledImage(ii.getImage(), Map.TILE_SIZE, Map.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/images/coin.png"));
        coinImage = Util.getScaledImage(ii.getImage(), Map.TILE_SIZE, Map.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/images/gem.png"));
        gemImage = Util.getScaledImage(ii.getImage(), Map.TILE_SIZE, Map.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/images/buttons/button-blue.png"));
        buttonImages[0][0] = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/images/buttons/button-blue-hover.png"));
        buttonImages[0][1] = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/images/buttons/button-red.png"));
        buttonImages[1][0] = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/images/buttons/button-red-hover.png"));
        buttonImages[1][1] = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/images/buttons/button-black.png"));
        buttonImages[2][0] = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));

        ii = new ImageIcon(getClass().getResource("/images/buttons/button-black-hover.png"));
        buttonImages[2][1] = new ImageIcon(Util.getScaledImage(ii.getImage(), 30, 30));
    }

}
