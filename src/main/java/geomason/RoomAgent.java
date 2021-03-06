package geomason;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.newdawn.slick.util.pathfinding.Mover;
import org.slf4j.LoggerFactory;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


/**
 * @author Norman Langner
 *
 */
public class RoomAgent extends MasonGeometry implements Steppable, Mover{ 
	
	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RoomAgent.class);

    private static final long serialVersionUID = -5318720825474063385L;

    public Point location = null; 
    private int moveRate;
    public static final int fakeAgentID = 999999;
    public static final int OWNCOST = 10000;
    public static enum Stadium {PREUSSEN,TEST,ESPRIT, TESTSMALL};
    private Room roomState = null;
    GeomVectorField accessableArea = null;
    private PointMoveTo pointMoveTo = new PointMoveTo();
    private int id;
    private Stoppable stoppMe;
    private int patienceCounter;
    private int maxMoveRate;
    private Tile destTile;
    private Results result;
	private int maxPatience;
	private boolean end = false;
	private ArrayList<java.awt.Point> trace = new ArrayList<java.awt.Point>();
	private Tile actualTile;
	private boolean hasToWait;
	private boolean deadlock;
	private boolean displayRecognized;

	private int deadlockCounter = 0;

	
    
    /**
     * 
     * Constructs a RoomAgent. 
     * @param id the id of RoomAgent 
     * @param moveRate the moveRate in tiles
     * @param maxMoveRate the maxMoveRate for this RoomAgent, defined by {@link Room} in tiles
     * @param maxPatience the maximum patience in steps for this RoomAgent 
     * @param destinationTile the destination tile where the RoomAgent wants to go
     * @param result the observer-Steppable {@link Results} which logs and handles the RoomAgents at the end
     */
    public RoomAgent(int id, int moveRate, int maxMoveRate, int maxPatience, Tile destinationTile, Results result){
       	this.id = id;
       	this.moveRate = moveRate;
       	this.maxMoveRate = maxMoveRate;
       	this.maxPatience = maxPatience;
       	this.destTile = destinationTile;
       	this.result = result;
    }
	
    public RoomAgent(){
    	//fakeagent
    }
    /* (non-Javadoc)
     * @see sim.engine.Steppable#step(sim.engine.SimState)
     */
    public void step(SimState state){
		roomState = (Room)state;
		deadlock = checkForDeadLock();
    	hasToWait = false;
    	for (int movestep=0; movestep<moveRate; movestep++){   		
    		moveAgent(roomState);
    		if (deadlock && moveRate%2 == 0){
    			return;
    		}
    		deadlock = false;
    		if (end || hasToWait) return;
    	}
    }
    
    /**
     * Moves the agent to the next tile which has the lowest costs,
     * is usable and free. If it is not, than
     * a counter is counted up. If this counter exceeds the value of
     * the maximum patience than the agent changes his destination
     * @param state the {@link SimState} where this object is initialized
     */
    private void moveAgent(SimState state){
    	// get the actual position
    	actualTile = getActualTile(roomState);
    	//if an agent is one of the destination areas, that means 
    	//he is safe and can be removed from the simulation
    	if (isTargetReached(actualTile)){
    		if (stoppMe == null){
    			throw new RuntimeException("Stoppable nicht gesetzt");
    		}
    		stopMeNow();
    		end = true;
    		return;
    	}
    	//if more than 30 steps no progress is made, maxPatience is reached 
    	if (deadlockCounter >= 3){
    		patienceCounter = maxPatience;
    	}
    	// if maxPatience is reached change the destination
    	if (patienceCounter == maxPatience){
    		randomlyChangeDestination();
    		patienceCounter = 0;
    		deadlockCounter = 0;
    	}
    	//if the agent makes no real progress, count how often that happens
    	if (deadlock){
    		deadlockCounter ++;
    		//randomlyChangeDestination();
			trace.clear();
		}
    	//calculate the tile with the least costs
    	Tile nextTile = getTileToMoveTo(actualTile, state);
    	if (nextTile == null){
    		patienceCounter++;
    		hasToWait = true;
    		return;
    	}
    	//move agent
    	if (nextTile != null){
    		patienceCounter = 0;
    		nextTile.addToPotentialList(this);
    		actualTile.removeFromPotentialList(this); 
    		java.awt.Point actPoint = new java.awt.Point(actualTile.getX(), actualTile.getY());
    		if (trace.size() >= 20){
    			trace.clear();
    		}
    		trace.add(actPoint);
        	Coordinate  coord = roomState.getCoordForTile(nextTile);
    		moveTo(coord);
    	}
    }

	private void randomlyChangeDestination() {
		Bag allDestTiles = roomState.getAllDestinationCenterTiles();
		Tile randomTile = (Tile) allDestTiles.get(roomState.random.nextInt(allDestTiles.size()));
		while (randomTile.equals(destTile) && roomState.getAllDestinationCenterTiles().size() > 1){
			randomTile = (Tile) allDestTiles.get(roomState.random.nextInt(allDestTiles.size()));
		}
		setDestTile(randomTile);
	}

	/**
	 * @return the trace as readable {@link String}
	 */
	private String logTrace() {
		StringBuffer sb = new StringBuffer();
		for (java.awt.Point p : trace) {
			sb.append("(");
			sb.append(p.x);
			sb.append(",");
			sb.append(p.y);
			sb.append("), ");
		}
		return sb.toString();
	}

	/**
	 * gets all tiles in the Moore-Neighbourhood, calculates which of those 
	 * has the cheapest costs, is empty and usable and returns that tile. If 
	 * this RoomAgent is in a deadlock, this method returns a random tile from 
	 * its Moore-Neighbourhood, which is empty and usable.
	 * @param actTile
	 * @param state
	 * @return the tile with the cheapest costs or if this Roomagent is in a deadlock, a random tile
	 */
	private Tile getTileToMoveTo(Tile actTile, SimState state) {
		Map<Tile, Integer> hmapWithTiles = new HashMap<Tile, Integer>();
		Bag neighbourTiles = roomState.allTilesOfMap.getObjectsWithinDistance(actTile.getGeometry().getCentroid(), Room.TILESIZE);
    	if (neighbourTiles.isEmpty()){
    		LOGGER.error("Fehler: Tile {} hat keine NachbarTiles", actTile);
    		throw new RuntimeException("Tile hat kein Nachbarn");
    	}
		neighbourTiles.remove(actTile);
    	for (Object o : neighbourTiles){
    		Tile tile = (Tile) o;
    		boolean inDestZone = roomState.getAllDestinationCenterTiles().contains(tile);
    		if (inDestZone){
    			return tile;
    		}
    		if (tile.isUsable() && tile.getPotentialAgentsList().isEmpty()){
    			int length = tile.getDestinations().get(destTile);
    			if (length != Integer.MAX_VALUE){
        			if (displayRecognized){
        				length = length + getCostsForTarget(tile, state) + tile.getAddCosts();
        			} else {
        				length = length + getCostsForTarget(tile, state);
        			}
    				hmapWithTiles.put(tile, length);    				
    			}
    		}
    		
    	}
    	hmapWithTiles = sortByValue(hmapWithTiles);
    	if (hmapWithTiles.isEmpty()){
    		calcPressure(neighbourTiles, state);
    		return null;
    	}
    	Tile shortest = hmapWithTiles.entrySet().iterator().next().getKey();
    	if (!deadlock || hmapWithTiles.size() == 1){
    		return shortest;
    	}
    	hmapWithTiles.remove(shortest);
    	boolean randomBool = false;
    	for (Map.Entry<Tile, Integer> entry : hmapWithTiles.entrySet()){
    		Tile key = entry.getKey();
    		randomBool = roomState.random.nextBoolean();
    		if (randomBool){
    			return key;
    		}
    	}
    	Tile nextshortest = hmapWithTiles.entrySet().iterator().next().getKey();
		return nextshortest;
	}
	 /**
	  * calculates the pressure for the tile, where it wants to go, but which is already
	  * occupied
	 * @param neighbourTiles the tiles in the Moore-Neighbourhood
	 * @param state the {@link SimState} of this RoomAgent
	 */
	private void calcPressure(Bag neighbourTiles, SimState state) {
		Map<Tile, Integer> hmapWithTiles = new HashMap<Tile, Integer>();
    	for (Object o : neighbourTiles){
    		Tile tile = (Tile) o;
    		if (tile.isUsable()){
    			int length = tile.getDestinations().get(destTile);
        		if (length != Integer.MAX_VALUE){
        			length = length + getCostsForTarget(tile, state);
        			hmapWithTiles.put(tile, length);
        		}
    		}
    		
    	}
    	if (hmapWithTiles.isEmpty()){
    		return;
    	}
    	hmapWithTiles = sortByValue(hmapWithTiles);
    	Tile shortest = hmapWithTiles.entrySet().iterator().next().getKey();
    	int length = hmapWithTiles.entrySet().iterator().next().getValue();
    	if (length >= 1000){
    		RoomAgent a = shortest.getPotentialAgentsList().get(0);
    		int pressure = moveRate;
    		result.addAgentToPressureList(a, pressure);
    	}
    		
    	
    	
	}

	/**
	 * checks the trace if a place is already visited more than 5 times
	 * if yes - true, else false
	 * @return {@link Boolean} 
	 */
	private boolean checkForDeadLock() {
		 if (trace.size()-11 >=0){
				List<java.awt.Point> sublist = trace.subList(trace.size()-11, trace.size()-1);
				int counter=0;
				for (int i=2; i< sublist.size();i++){
					int ax,ay,bx,by, cx,cy;
					ax = ((java.awt.Point) sublist.get(i)).x;
					ay = ((java.awt.Point) sublist.get(i)).y;
					bx = ((java.awt.Point) sublist.get(i-1)).x;
					by = ((java.awt.Point) sublist.get(i-1)).y;
					cx = ((java.awt.Point) sublist.get(i-2)).x;
					cy = ((java.awt.Point) sublist.get(i-2)).y;
					if ( (ax == bx && ay== by) ||(ax == cx && ay == cy)){
						counter++;
						if (counter>=5){
							return true; 
						}
					}
				}

		 }
		 return false;
		
	}

	/**
	 * sort for generic maps, which sorts the map by value 
	 * @param map to sort by value
	 * @return {@link LinkedHashMap}
	 */
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
	     java.util.List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
	     Collections.sort( list, new Comparator<Map.Entry<K, V>>(){
	         public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ){
	             return (o1.getValue()).compareTo( o2.getValue() );
	         }
	     } );
	     Map<K, V> result = new LinkedHashMap<K, V>();
	     for (Map.Entry<K, V> entry : list) {
	         result.put( entry.getKey(), entry.getValue() );
	     }
	     return result;
	 }
	
   

	
    /**
     * calculates the cost for the target tile including influences of other 
     * RoomAgents on the target tile
     * @param tile the target tile
     * @param state the {@link SimState} of this {@link RoomAgent}
     * @return the costs as {@link Integer}
     */
    private int getCostsForTarget(Tile tile, SimState state) {
		Tile targetTile = tile;
		int costs = 0;
		Bag agents = getAgentsInMaxMoveRateDistance(targetTile);
		if (agents.isEmpty() || roomState.getAllTilesOfDestinations().contains(targetTile)){
			costs = roomState.getStandardCostsForTargetTile(actualTile, targetTile, costs);
		} else {
			for (Object o : agents){
				RoomAgent agent = (RoomAgent) o;
				Tile agentPos = agent.getPositionAsTile(state);
				costs = roomState.getStandardCostsForTargetTile(agentPos, targetTile, costs);
			}
		}
		return costs;
	}
    
	/**
	 * get all agents which are in a radius of maxMoveDistance
	 * @param actTile the actual postion as tile
	 * @return {@link Bag} 
	 */
	private Bag getAgentsInMaxMoveRateDistance(Tile actTile) {
		Bag agentBag = Room.agents.getObjectsWithinDistance(actTile.getGeometry().getCentroid(), maxMoveRate*Room.TILESIZE);	
		return agentBag;
	}


    // move agent to a certain coordinate
    private void moveTo(Coordinate c)
    {
        pointMoveTo.setCoordinate(c);
        location.apply(pointMoveTo);
    }

    
	
	private Tile getActualTile(SimState state) {
		roomState = (Room) state;
		Tile t = roomState.getTileByCoord(location.getX(), location.getY());
		return t;
	}
    
	/**
	 * Returns the actual position of the {@link RoomAgent} as a {@link Tile}
	 * @param state the {@link SimState} the agent is in 
	 * @return the position of this {@link RoomAgent} as a {@link Tile}
	 */
	public Tile getPositionAsTile(SimState state){
		if (actualTile==null){
			actualTile = getActualTile(state);
		}
		return actualTile;
	}

    private boolean isTargetReached(Tile actTile) {
    	boolean equals = destTile.equals(actTile);
    	boolean compareValues = false;
    	boolean targetReached = false;
    	if (actTile.getX() == destTile.getX() && actTile.getY() == destTile.getY()){
    		compareValues = true;
    	}
    	if (equals || compareValues){
    		targetReached = true;
    	}
    	if (roomState.getAllTilesOfDestinations().contains(actTile)){
    		targetReached = true;
    	}
    	return targetReached;
	}
  
	
    /**
     * Sets the position of the agent in the simulation
     * @param p the position of the agent in the simulation
     */
    public void setLocation(Point p){ 
    	location = p; 
    	this.geometry = p;
    }

    /* (non-Javadoc)
     * @see sim.util.geo.MasonGeometry#getGeometry()
     */
    public Geometry getGeometry(){ 
    	return location;
    }
    
    

	/* (non-Javadoc)
	 * @see sim.util.geo.MasonGeometry#toString()
	 */
	@Override
	public String toString() {
		return String.format("RoomAgent [location=%s, moveRate=%s, id=%s]",
				location, moveRate, id);
	}

	/**
	 * Returns the id of an agent. This is for example needed to get which agent died.
	 * @return the id of the agent
	 */
	public int getId() {
		return id;
	}
	
	
	/**
	 * This method is for the observer to stop those agents which have reached their destinations or
	 * are dead to get them removed from the simulation. If that is not done, the simulations runs 
	 * forever.
	 */
	public void stopMeNow(){
		result.reduceAgents();
		actualTile.removeFromPotentialList(this);
		stoppMe.stop();
	}
	/**
	 * Sets the {@link Stoppable} for this agent, which allows to stop the agent and get him removed 
	 * from the simulation.
	 * @param stoppMe the {@link Stoppable} to set
	 */
	public void setStoppMe(Stoppable stoppMe) {
		this.stoppMe = stoppMe;
	}

	/**
	 * Returns the max move rate in tiles per step of this agent
	 * @return the max move rate
	 */
	public int getMaxMoveRate() {
		return maxMoveRate;
	}

	/**
	 * Sets the max move rate in tiles per step of this agent. 
	 * @param maxMoveRate the max move rate 
	 */
	public void setMaxMoveRate(int maxMoveRate) {
		this.maxMoveRate = maxMoveRate;
	}

	/**
	 * Returns the destination {@link Tile} of this agent.
	 * @return the destTile
	 */
	public Tile getDestTile() {
		return destTile;
	}

	/**
	 * Sets the destination {@link Tile} of this agent
	 * @param destTile the destTile to set
	 */
	public void setDestTile(Tile destTile) {
		this.destTile = destTile;
	}

	/**
	 * Returns the actual move rate of this agent
	 * @return the moveRate
	 */
	public int getMoveRate() {
		return moveRate;
	}

	/**
	 * Sets the move rate of this agent. Usually between 1-5
	 * @param moveRate the moveRate to set
	 */
	public void setMoveRate(int moveRate) {
		this.moveRate = moveRate;
	}

	/**
	 * Returns if a display is recognized by this agent
	 * @return the displayRecognized
	 */
	public boolean isDisplayRecognized() {
		return displayRecognized;
	}

	/**
	 * Sets if a display was recognized by this agent
	 * @param displayRecognized the displayRecognized to set
	 */
	public void setDisplayRecognized(boolean displayRecognized) {
		this.displayRecognized = displayRecognized;
	}

	/**
	 * Returns the patience counter
	 * @return the patienceCounter
	 */
	public int getPatienceCounter() {
		return patienceCounter;
	}

	/**
	 * Returns if the agent is in a deadlock
	 * @return the deadlock
	 */
	public boolean isDeadlock() {
		return deadlock;
	}
	    
}
