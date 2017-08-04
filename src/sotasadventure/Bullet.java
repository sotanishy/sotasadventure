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

    public boolean alive;

    public Bullet(double firedTime) {
        this.firedTime = firedTime;
        this.alive = true;
    }

    /**
     * Moves a bullet
     * @param elapsedTime the time elapsed since the game started
     */
     public void move(double elapsedTime) {
         if (elapsedTime - firedTime > DURATION) {
             alive = false;
         }

         position.x += speed;
    }
}
