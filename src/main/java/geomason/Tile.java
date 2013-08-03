package geomason;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author slayer
 *
 */
public class Tile extends MasonGeometry{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5161567811618409640L;
	private ArrayList<Agent> agentList = new ArrayList<Agent>();
	private ArrayList<RoomAgent> potentialRoomAgentList = new ArrayList<RoomAgent>();
	private boolean usable = false;
	private Polygon polygon = null;
	private int x,y;
	private HashMap<Tile, Integer> destinations = new HashMap<Tile, Integer>();
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Tile.class);
	
	public Tile(int x, int y) {
		super();
		this.x = x;
		this.y = y;
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
	
	
	public void addAgent(Agent a){
		agentList.add(a);
	}
	
	public void addAgents(ArrayList<Agent> al){
		agentList.addAll(al);
	}
	public void removeAgent(Agent a){
		agentList.remove(a);
	}
	
	public void removeAgents(ArrayList<Agent> al){
		agentList.removeAll(al);
	}

	/**
	 * @return the agentList
	 */
	public ArrayList<Agent> getAgentList() {
		return agentList;
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
	public void addToPotentialList(RoomAgent a){
		potentialRoomAgentList.add(a);
		//LOGGER.info("Roomagent mit ID {} zu {} hinzugefügt", a.getId(),this);
	}
	
	public void removeFromPotentialList(RoomAgent a){
		potentialRoomAgentList.remove(a);
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
				.format("Tile [agentList=%s, potentialRoomAgentList=%s, usable=%s, polygon=%s, x=%s, y=%s]",
						agentList, potentialRoomAgentList, usable, polygon, x, y);
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
	
}
