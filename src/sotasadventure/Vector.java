package sotasadventure;

/**
 * The class that represents and operates vectors.
 * @author Sota Nishiyama
 */
public class Vector {
    public int x;
    public int y;

    /**
     * Constructs an empty vector.
     */
    public Vector() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Constructs a vector with x and y values.
     * @param x the x value
     * @param y the y value
     */
    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Adds another vector to this vector.
     * @param v the addend vector
     */
    public void add(Vector v) {
        this.x += v.x;
        this.y += v.y;
    }
}
