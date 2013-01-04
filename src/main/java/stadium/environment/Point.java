package stadium.environment;

public class Point {

	private int x,y, flow, capacity;
//TODO: von Point2d ableiten
	private String type;
	/**
	 * @param args
	 */
/**
 * @param x 
 * @param y
 * @param f
 * @param c
 * @param t
 */
public Point(int x, int y, int f, int c, String t){
	try {
		setX(x);
		setY(y);
		setFlow(f);
		setCapacity(c);
		setType(t);
	} catch (NullPointerException e) {
		e.printStackTrace();
	}

}
	
	private void setX(int x) {
		this.x = x;
	}
	
	private void setY(int y) {
		this.y = y;
	}
	
	private void setFlow(int flow) {
		this.flow = flow;
	}
	
	private void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	private void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public int getCapacity() {
		return capacity;
	}
	public int getFlow() {
		return flow;
	}
	public int getY() {
		return y;
	}
	public int getX() {
		return x;
	}
}
