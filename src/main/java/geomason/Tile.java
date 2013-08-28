package geomason;

import java.util.ArrayList;
import java.util.HashMap;

import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Norman Langner
 *
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
	
	public Tile(int x, int y, int addCosts) {
		super();
		this.x = x;
		this.y = y;
		this.addCosts = addCosts;
	}

	public void setPolygon(Polygon p){
		polygon = p;
		polygon.convexHull();
		this.geometry = p;
	}
	
	public Geometry getGeometry(){
		return polygon;
	}
	
	public void setUsable(boolean u){
		usable = u;
	}
	
	
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
	
	public boolean isInTile(Point p){
		boolean b = polygon.covers(p);
		return b;
	}
	
	
	//für room
	public synchronized void addToPotentialList(RoomAgent a){
		potentialRoomAgentList.add(a);
		//LOGGER.debug("Roomagent mit ID {} zu Tile({},{}) hinzugefügt", a.getId(),getX(),getY());
	}
	
	public synchronized void removeFromPotentialList(RoomAgent a){
		potentialRoomAgentList.remove(a);
		//LOGGER.debug("Roomagent mit ID {} von Tile({},{}) entfernt", a.getId(),getX(),getY());
	}
	
	/**
	 * @return the agentList
	 */
	public ArrayList<RoomAgent> getPotentialAgentsList() {
		return potentialRoomAgentList;
	}

	@Override
	public String toString() {
		return String
				.format("Tile(x=%s, y=%s)",
						x, y);
	}

	/**
	 * @return the destinations
	 */
	public HashMap<Tile, Integer> getDestinations() {
		return destinations;
	}

	/**
	 * @param destinations the destinations to set
	 */
	public void setDestinations(HashMap<Tile, Integer> destinations) {
		this.destinations = destinations;
	}

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
	 * @return the addCosts
	 */
	public int getAddCosts() {
		return addCosts;
	}

	/**
	 * @param addCosts the addCosts to set
	 */
	public void setAddCosts(int addCosts) {
		this.addCosts = addCosts;
	}
	
	
	
}
