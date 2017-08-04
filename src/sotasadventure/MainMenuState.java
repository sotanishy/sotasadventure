package sotasadventure;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * The class that shows the main menu.
 * @author Sota Nishiyama
 */
public class MainMenuState extends State {

    private StateMachine gameMode;

    private Image background;

    private ImageIcon logo;
    private ImageIcon startIcon;
    private ImageIcon startIconHover;

    /**
     * Sets the layout and loads images.
     * @param gameMode the state machine of the game
     */
    public MainMenuState(StateMachine gameMode) {
        this.gameMode = gameMode;

        loadResources();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel title = new JLabel(logo);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JButton start = new JButton(startIcon);
        start.setAlignmentX(CENTER_ALIGNMENT);
        start.setBorder(BorderFactory.createEmptyBorder());
        start.setContentAreaFilled(false);
        start.setIcon(startIcon);
        start.setRolloverEnabled(true);
        start.setRolloverIcon(startIconHover);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMode.change("worldmap");
            }
        });

        add(Box.createRigidArea(new Dimension(0, 100)));
        add(title);
        add(Box.createRigidArea(new Dimension(0, 100)));
        add(start);
    }

    @Override
    public void update(double elapsedTime) {}

    @Override
    public void render() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(Util.getScaledImage(background, (int) getSize().getWidth(), (int) getSize().getHeight()), 0, 0, null);
    }

    @Override
    public void enter(String optional) {}

    @Override
    public void exit() {}

    /**
     * Loads images.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/mainmenu-bg.png"));
        background = ii.getImage();

        ii = new ImageIcon(getClass().getResource("/resources/images/logo.png"));
        logo = new ImageIcon(Util.getScaledImage(ii.getImage(), 640, 267));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/start_button.png"));
        startIcon = new ImageIcon(Util.getScaledImage(ii.getImage(), 200, 100));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/start_button_hover.png"));
        startIconHover = new ImageIcon(Util.getScaledImage(ii.getImage(), 200, 100));
    }
}
