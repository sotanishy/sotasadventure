package sotasadventure;

/**
 * The class that represents a bullet.
 */

public class Bullet {
    public static final float DURATION = (float) .7;
    public static final float SUCCESSION = (float) .6;

    public Vector position = new Vector();
    public int speed;
    public int damage = 1;
    public float firedTime;
}