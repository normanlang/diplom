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
    private int destinationChanger;
    private int maxMoveRate;
    private Tile destTile;
    private Results result;
	private int maxPatience;
	private boolean end = false;
	private ArrayList<java.awt.Point> trace = new ArrayList<java.awt.Point>();
	private Tile actualTile;
	boolean destchanger;
	
    
    public RoomAgent(int id, int moveRate, int maxMoveRate, int maxPatience, Tile destinationTile, Results result){
       	this.id = id;
       	this.moveRate = moveRate;
       	this.maxMoveRate = maxMoveRate;
       	this.maxPatience = maxPatience;
       	this.destTile = destinationTile;
       	this.result = result;
    }
		
    public void step(SimState state){
		roomState = (Room)state; 
        //weiter gehts
    	destchanger = false;
    	for (int movestep=0; movestep<moveRate; movestep++){
    		moveAgent(roomState);
    		if (end || destchanger) return;
    	}
    }
    
    private void moveAgent(SimState state){
    	actualTile = getActualTile(roomState);
    	if (isTargetReached(actualTile)){
//    		LOGGER.info("Agent {} hat Ziel erreicht (MoveRate:{}, Ziel:({},{}),Steps:{})",
//    					id,
//    					moveRate,
//    					destTile.getX(),
//    					destTile.getY(),
//    					String.valueOf(roomState.schedule.getSteps()));
    		if (stoppMe == null){
    			throw new RuntimeException("Stoppable nicht gesetzt");
    		}
    		stopMeNow();
    		end = true;
    		if (roomState.NUM_AGENTS <= 150){
    			LOGGER.info("Trace: {}", logTrace());
    		}
    		return;
    	}
    	if (destinationChanger == maxPatience || checkForDeadLock() ){
    		Bag allDestTiles = roomState.getAllDestinationCenterTiles();
    		Tile randomTile = (Tile) allDestTiles.get(roomState.random.nextInt(allDestTiles.size()));
    		while (randomTile.equals(destTile) && roomState.getAllDestinationCenterTiles().size() > 1){
    			randomTile = (Tile) allDestTiles.get(roomState.random.nextInt(allDestTiles.size()));
    		}
    		setDestTile(randomTile);
//    		LOGGER.info("Agent {} hat Ziel gewechselt (aktPos:({},{}), Ziel:({},{}),Steps:{})",
//					id,
//					actualTile.getX(),
//					actualTile.getY(),
//					destTile.getX(),
//					destTile.getY(),
//					String.valueOf(state.schedule.getSteps()));
    		destinationChanger = 0;
    	}

    	Tile nextTile = getTileToMoveTo(actualTile, state);
    	if (nextTile == null){
    		destinationChanger++;
    		destchanger = true;
    		return;
    	}
    	if (nextTile != null){
    		destinationChanger = 0;
    		nextTile.addToPotentialList(this);
    		actualTile.removeFromPotentialList(this); 
    		java.awt.Point actPoint = new java.awt.Point(actualTile.getX(), actualTile.getY());
    		trace.add(actPoint);
        	Coordinate  coord = roomState.getCoordForTile(nextTile);
//        	if (roomState.getAllTilesOfDestinations().contains(nextTile)){
//        		LOGGER.info("Agent {} von ({},{}) nach ({},{}) - Ziel:({},{})",
//        				id,
//        				actualTile.getX(),
//        				actualTile.getY(),
//        				nextTile.getX(),
//        				nextTile.getY(),
//        				String.valueOf(destTile.getX()),
//        				String.valueOf(destTile.getY()));
//        	}
    		moveTo(coord);
    	}
    }

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
    				length = length + getCostsForTarget(tile, state);
    				hmapWithTiles.put(tile, length);
//    				if (state.schedule.getSteps() >= 300){
//    					System.out.print("("+tile.getX()+","+tile.getY()+")->"+length+" ;");
//    				}
    				
    			}
    		}
    		
    	}
//		if (state.schedule.getSteps() >= 300){
//			System.out.println("");
//		}
    	hmapWithTiles = sortByValue(hmapWithTiles);
    	if (hmapWithTiles.isEmpty()){
    		calcPressure(neighbourTiles, state);
    		return null;
    	}
    	Tile shortest = hmapWithTiles.entrySet().iterator().next().getKey();
		
//    	LOGGER.debug("Kürzester Weg für Agent {}: ({},{}),Kosten:{},besetzt durch {}",
//    	d		shortest.getPotentialAgentsList());
		return shortest;
	}
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
    
	private Bag getAgentsInMaxMoveRateDistance(Tile actTile) {
		Bag agentBag = Room.agents.getObjectsWithinDistance(actTile.getGeometry().getCentroid(), maxMoveRate*Room.TILESIZE);	
		return agentBag;
	}


    // bewegt den Agenten zu den gegebenen Koordinaten
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
    	return targetReached;
	}
  
	
    public void setLocation(Point p){ 
    	location = p; 
    	this.geometry = p;
    }

    public Geometry getGeometry(){ 
    	return location;
    }
    
    

	@Override
	public String toString() {
		return String.format("RoomAgent [location=%s, moveRate=%s, id=%s]",
				location, moveRate, id);
	}

	public int getId() {
		return id;
	}
	
	
	public void stopMeNow(){
		result.reduceAgents();
		actualTile.removeFromPotentialList(this);
		stoppMe.stop();
	}
	/**
	 * @param stoppMe the stoppMe to set
	 */
	public void setStoppMe(Stoppable stoppMe) {
		this.stoppMe = stoppMe;
	}

	/**
	 * @return the maxMoveRate
	 */
	public int getMaxMoveRate() {
		return maxMoveRate;
	}

	/**
	 * @param maxMoveRate the maxMoveRate to set
	 */
	public void setMaxMoveRate(int maxMoveRate) {
		this.maxMoveRate = maxMoveRate;
	}

	/**
	 * @return the destTile
	 */
	public Tile getDestTile() {
		return destTile;
	}

	/**
	 * @param destTile the destTile to set
	 */
	public void setDestTile(Tile destTile) {
		this.destTile = destTile;
	}

	/**
	 * @return the moveRate
	 */
	public int getMoveRate() {
		return moveRate;
	}

	/**
	 * @param moveRate the moveRate to set
	 */
	public void setMoveRate(int moveRate) {
		this.moveRate = moveRate;
	}
	
}
