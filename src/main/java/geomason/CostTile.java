package geomason;

/**
 * @author Norman Langner
 * This class represents the additional costs for each tile which form the dynamic floor field
 */
public class CostTile {

	private int x,y;
	private int costs = 1;
	
	/**
	 * The constructor sets the position of the CostTile and its initial costs
	 * @param x
	 * @param y
	 * @param costs
	 */
	public CostTile(int x, int y, int costs){
		this.x = x;
		this.y = y;
		this.costs = costs;
	}

	/**
	 * @return the x-Position in the tile-map
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y-Position in the tile-map
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the costs (min 1)
	 */
	public int getCosts() {
		return costs;
	}

	/**
	 * @param costs the costs to set
	 */
	public void addCosts(int costs) {
		this.costs = this.costs + costs;
	}
	
	/**
	 * resets the costs to 1
	 */
	public void resetCosts(){
		this.costs = 1;
	}

}
