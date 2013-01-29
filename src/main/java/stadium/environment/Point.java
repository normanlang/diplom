package stadium.environment;



/**
 * @author Norman Langner
 *
 * This class defines a Point with the help of the java.awt.Point class which gets extended 
 * with new attributes flow, capacity and type. The class is an Immutable.
 */
final public class Point extends java.awt.Point{

	private static final long serialVersionUID = 2971754724453076253L;
	final private int x,y;
	final private int flow, capacity;
	final private String type;
/**
 * @param x 
 * @param y
 * @param f = flow
 * @param c = capacity 
 * @param t = type
 */
public Point(int x, int y, int f, int c, String t){
		this.flow = f;
		this.capacity = c;
		this.type = t;
		this.x = x;
		this.y = y;
}

	/**
	 * gets the type of the point as String (crossroad, exit, entrance, dead end)
	 * @return type  
	 */
	public final String getType() {
		return type;
	}
	/**
	 * gets the maximum possible capacity of the point as Integer
	 * @return maximum possible capacity
	 */
	public final int getCapacity() {
		return capacity;
	}
	/**
	 * gets the maximum possible flow of the people at this Point
	 * @return maximum possible flow
	 */
	public final int getFlow() {
		return flow;
	}
	
	
}
