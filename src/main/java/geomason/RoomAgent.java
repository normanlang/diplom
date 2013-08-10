package geomason;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;
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
    public static final int OWNCOST = 1000;
    public static enum Stadium {PREUSSEN,TEST,ESPRIT};
    private Stadium stadium;
    private Path path;
    private Room roomState = null;
    private ArrayList<Tile>  pathAsTileList = new ArrayList<Tile>();
    GeomVectorField accessableArea = null;
    private PointMoveTo pointMoveTo = new PointMoveTo();
    private int step = 0;
    private int id;
    private Stoppable stoppMe;
    private int destinationChanger;
    private int maxMoveRate;
    private Tile destTile;
    private Results result;
	private int maxPatience;
	private boolean end = false;
	boolean destchanger;
    
    public RoomAgent(int id, Stadium stadium, int moveRate, int maxMoveRate, int maxPatience, Tile destinationTile, Results result){
       	this.stadium = stadium;
       	this.id = id;
       	this.moveRate = moveRate;
       	this.maxMoveRate = maxMoveRate;
       	this.maxPatience = maxPatience;
       	this.destTile = destinationTile;
       	this.result = result;
    }
		
    public void step(SimState state){
        //weiter gehts
    	setStateDependingOnStadium(state);
    	//moveAgentOnPath(state);
    	destchanger = false;
    	for (int movestep=0; movestep<moveRate; movestep++){
    		moveAgent(state);
    		if (end || destchanger) return;
    	}
    	//System.out.println("");
    }
    
    private void moveAgent(SimState state){
    	Tile actTile = getActualTile(state);
    	if (isTargetReached(actTile)){
    		LOGGER.info("Agent {} hat Ziel erreicht (MoveRate:{}, Ziel:({},{}),Steps:{})",
    					id,
    					moveRate,
    					destTile.getX(),
    					destTile.getY(),
    					String.valueOf(state.schedule.getSteps()));
    		if (stoppMe == null){
    			throw new RuntimeException("Stoppable nicht gesetzt");
    		}
    		result.reduceAgents();
    		stoppMe.stop();
    		end = true;
    		return;
    	}
    	if (destinationChanger == maxPatience ){
    		Bag allDestTiles = roomState.getAllDestinationCenterTiles();
    		Tile randomTile = (Tile) allDestTiles.get(roomState.random.nextInt(allDestTiles.size()));
    		setDestTile(randomTile);
    		LOGGER.info("Agent {} hat Ziel gewechselt (MoveRate:{}, Ziel:({},{}),Steps:{})",
					id,
					moveRate,
					destTile.getX(),
					destTile.getY(),
					String.valueOf(state.schedule.getSteps()));
    		destinationChanger = 0;
    	}

    	Tile nextTile = getTileToMoveTo(actTile, state);
    	if (!(nextTile.getPotentialAgentsList().isEmpty()) && !(roomState.getAllDestinationCenterTiles().contains(nextTile))){
    		destinationChanger++;
    		destchanger = true;
    		return;
    	}
    	destinationChanger = 0;
		nextTile.addToPotentialList(this);
		actTile.removeFromPotentialList(this);    	
    	Coordinate  coord = roomState.getCoordForTile(nextTile);
    	if (roomState.getAllTilesOfDestinations().contains(nextTile)){
    		LOGGER.info("Agent {} von ({},{}) nach ({},{}) - Ziel:({},{})",
    				id,
    				actTile.getX(),
    				actTile.getY(),
    				nextTile.getX(),
    				nextTile.getY(),
    				String.valueOf(destTile.getX()),
    				String.valueOf(destTile.getY()));
    	}
		moveTo(coord);
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
    		if (tile.isUsable()){
    			int length = tile.getDestinations().get(destTile);
    			if (length != Integer.MAX_VALUE){
    				length = length * this.getCostsForTarget(tile, state);
    				hmapWithTiles.put(tile, length);
    			}
    		}
    		
    	}
    	hmapWithTiles = sortByValue(hmapWithTiles);
		return hmapWithTiles.entrySet().iterator().next().getKey();
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
		int costs = 1;
		Bag agents = getAgentsInMaxMoveRateDistance(targetTile);
		if (agents.isEmpty()){
			costs = roomState.getStandardCostsForTargetTile(getActualTile(state), targetTile, costs);
		} else {
			for (Object o : agents){
				RoomAgent agent = (RoomAgent) o;
				costs = roomState.getStandardCostsForTargetTile(agent.getActualTile(state), targetTile, costs);
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
//	private void moveAgentOnPath(SimState state){
//    	if (isTargetReachedOnPath()){
//    		LOGGER.info("Agent {} hat Ziel erreicht",this);
//    		if (stoppMe == null){
//    			throw new RuntimeException("Stoppable nicht gesetzt");
//    		}
//    		stoppMe.stop();
//    		return;
//    	}
//    	if(isAnyTileInViewDistanceBlocked()){
//    		Tile start = pathAsTileList.get(step);
//    		Tile end = pathAsTileList.get(pathAsTileList.size()-1);
//    		Path p = roomState.calcNewPath(this, start, end);
//    		if (p == null){
//    			destinationChanger++;
//    			return;
//    		}
//    		setPath(state, p);
//    	}
//    	Tile nextTile = getNextTileOnPath();
//    	nextTile.addToPotentialList(this);
//		getActTileOnPath().removeFromPotentialList(this);
//    	Coordinate  coord = roomState.getCoordForTile(nextTile);
//		moveTo(coord);
//		step += moveRate;
//    }
//
//	private boolean isAnyTileInViewDistanceBlocked() {
//		for (int i= step; i<step+maxMoveRate; i++){
//			Tile t = pathAsTileList.get(i);
//			if (roomState.isBlocked(this, t)){
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private Tile getActTileOnPath() {
//		return pathAsTileList.get(step);
//	}
//
//	private Tile getNextTileOnPath() {
//		if (step+moveRate >= pathAsTileList.size()){
//			return pathAsTileList.get(pathAsTileList.size()-1);
//		}
//		return pathAsTileList.get(step+moveRate);
//	}
//
//	private boolean isTargetReachedOnPath() {
//		return step>=pathAsTileList.size()-1;
//	}
//	/**
//	 * @return the path
//	 */
//	public Path getPath() {
//		return path;
//	}
//
//	/**
//	 * @param path the path to set
//	 */
//	public void setPath(SimState state, Path p) {
//		setStateDependingOnStadium(state);
//		step = 0;
//		if (p!=null){
//			this.path = p;
//			//packe alle tiles in eine arraylist zum einfachen arbeiten
//			setPathAsTileList();
//		}
//	}  
//    private void setPathAsTileList(){
//    	pathAsTileList = new ArrayList<Tile>(); //TODO: evtl .clear() machen, wenn ram voll läuft
//    	for (int i=0; i< path.getLength();i++){
//			Step step = path.getStep(i);
//			Tile t = roomState.getTile(step.getX(), step.getY());
//			pathAsTileList.add(i, t);
//		}
//    }
    
	private void setStateDependingOnStadium(SimState state){
    	switch (stadium){
    	case PREUSSEN: //für Preussenstadion
    		PreussenStadiumModel preussenStadiumModelState = (PreussenStadiumModel)state; 
            accessableArea = preussenStadiumModelState.movingSpace;
            break;
    	case TEST:         //für Testroom
    		roomState = (Room)state; 
            accessableArea = roomState.movingSpace;
            break;
    	case ESPRIT: 
    		System.out.println("noch nicht fertig");
    		break;
    	default: 
    		System.out.println("es wurde kein Stadium ausgewählt");
    		break;
    	}
	}
	
	private Tile getActualTile(SimState state) {
		setStateDependingOnStadium(state);
		return roomState.getTileByCoord(location.getX(), location.getY());
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
