package geomason;

import java.util.ArrayList;
import java.util.HashMap;

import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Norman Langner
 * This class models a tile of the static floor field with all its properties
 */
public class Tile extends MasonGeometry{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5161567811618409640L;
	private ArrayList<RoomAgent> potentialRoomAgentList = new ArrayList<RoomAgent>();
	private boolean usable = false;
	private Polygon polygon = null;
	private int x,y;
	private HashMap<Tile, Integer> destinations = new HashMap<Tile, Integer>();
	private int addCosts;

	
//	private static final Logger LOGGER = LoggerFactory.getLogger(Tile.class);
	
	/**
	 * Constructor needs the position of the tile (x,y) and the additional costs at start of the simulation for this tile.
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param addCosts the additional costs
	 */
	public Tile(int x, int y, int addCosts) {
		super();
		this.x = x;
		this.y = y;
		this.addCosts = addCosts;
	}
	/**
	 * The polygon is needed, to use all of the features of jts, mason and geomason. 
	 * @param p the Polygon which describes the tile
	 */
	public void setPolygon(Polygon p){
		polygon = p;
		polygon.convexHull();
		this.geometry = p;
	}
	/* (non-Javadoc)
	 * @see sim.util.geo.MasonGeometry#getGeometry()
	 */
	public Geometry getGeometry(){
		return polygon;
	}
	/**
	 * Sets if the tile is usable for agents or not.
	 * @param u
	 */
	public void setUsable(boolean u){
		usable = u;
	}
	
	/**
	 * Returns if this tile is usable or not for agents
	 * @return true/false
	 */
	public boolean isUsable(){
		return usable;
	}
	
	/**
	 * @return x the x in the map matrix
	 */
	public int getX(){
		return x;
	}
	
	/**
	 * @return y the y in the map matrix
	 */
	public int getY(){
		return y;
	}
	/**
	 * Returns if given point p is in tile
	 * @param p a point
	 * @return boolean, if given point p is in tile
	 */
	public boolean isInTile(Point p){
		boolean b = polygon.covers(p);
		return b;
	}
	
	
	//für room
	/**
	 * Adds an agent to the "queue" of agents who potentially want to get into this tile
	 * @param a an agent
	 */
	public synchronized void addToPotentialList(RoomAgent a){
		potentialRoomAgentList.add(a);
		//LOGGER.debug("Roomagent mit ID {} zu Tile({},{}) hinzugefügt", a.getId(),getX(),getY());
	}
	/**
	 * Removes an agent of the "queue" of agents who potentially want to get into this tile 
	 * @param a, the agent which will be removed
	 */
	public synchronized void removeFromPotentialList(RoomAgent a){
		potentialRoomAgentList.remove(a);
		//LOGGER.debug("Roomagent mit ID {} von Tile({},{}) entfernt", a.getId(),getX(),getY());
	}
	
	/**
	 * Returns the "queue" of agents who potentially want to get into this tile
	 * @return the agentList
	 */
	public ArrayList<RoomAgent> getPotentialAgentsList() {
		return potentialRoomAgentList;
	}

	/* (non-Javadoc)
	 * @see sim.util.geo.MasonGeometry#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("Tile(x=%s, y=%s)",
						x, y);
	}

	/**
	 * Returns a {@link HashMap} of all destinations with their distance from this tile to the destination
	 * @return the destinations
	 */
	public HashMap<Tile, Integer> getDestinations() {
		return destinations;
	}

	/**
	 * Returns a {@link HashMap} of all destinations with their distance from this tile to the destination
	 * @param destinations the destinations to set
	 */
	public void setDestinations(HashMap<Tile, Integer> destinations) {
		this.destinations = destinations;
	}
	/**
	 * Adds a destination with its distance from this tile to the destination to the destinations list
	 * @param tile
	 * @param length
	 */
	public void addDestination(Tile tile, int length) {
		destinations.put(tile, length);
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	/**
	 * Returns the additional costs of this tile
	 * @return the addCosts
	 */
	public int getAddCosts() {
		return addCosts;
	}

	/**
	 * Sets the additional costs for this tile
	 * @param addCosts the addCosts to set
	 */
	public void setAddCosts(int addCosts) {
		this.addCosts = addCosts;
	}
	
	
	
}
