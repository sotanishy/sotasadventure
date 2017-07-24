package sotasadventure;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private ImageIcon startImage;
    private ImageIcon startImageHover;

    /**
     * Sets the layout and loads images.
     * @param gameMode the state machine of the game
     */
    public MainMenuState(StateMachine gameMode) {
        this.gameMode = gameMode;

        loadImages();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // label
        JLabel title = new JLabel(logo);
        title.setAlignmentX(CENTER_ALIGNMENT);

        // button
        JButton start = new JButton(startImage);
        start.setBorder(BorderFactory.createEmptyBorder());
        start.setContentAreaFilled(false);
        start.setAlignmentX(CENTER_ALIGNMENT);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMode.change("worldmap");
            }
        });
        start.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                start.setIcon(startImageHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                start.setIcon(startImage);
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
    public void enter() {}

    @Override
    public void exit() {}

    /**
     * Loads images of contents in the main menu.
     */
    private void loadImages() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/mainmenu-bg.png"));
        background = ii.getImage();

        ii = new ImageIcon(getClass().getResource("/resources/images/logo.png"));
        logo = new ImageIcon(Util.getScaledImage(ii.getImage(), 640, 267));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/start_button.png"));
        startImage = new ImageIcon(Util.getScaledImage(ii.getImage(), 200, 100));

        ii = new ImageIcon(getClass().getResource("/resources/images/buttons/start_button_hover.png"));
        startImageHover = new ImageIcon(Util.getScaledImage(ii.getImage(), 200, 100));
    }
}
