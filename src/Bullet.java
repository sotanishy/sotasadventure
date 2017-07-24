package sotasadventure;

/**
 * The class that represents a bullet.
 * @author Sota Nishiyama
 */
public class Bullet {
    public static final double DURATION = .7;
    public static final double SUCCESSION = .6;

    public Vector position = new Vector();
    public int speed;
    public int damage = 1;
    public double firedTime;
}
