package geomason;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;
import org.slf4j.LoggerFactory;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.PointMoveTo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


public class RoomAgent implements Steppable, Mover{
	
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
    
    public RoomAgent(int id, Stadium stadium, int moveRate, int maxMoveRate, Tile destinationTile){
       	this.stadium = stadium;
       	this.id = id;
       	this.moveRate = moveRate;
       	this.maxMoveRate = maxMoveRate;
       	this.destTile = destinationTile;
    }
		
    public void step(SimState state){
        //weiter gehts
    	setStateDependingOnStadium(state);
    	moveAgentOnPath(state);
    }
    
    private void moveAgent(SimState state){
    	if (isTargetReached()){
    		LOGGER.info("Agent {} hat Ziel erreicht",this);
    		if (stoppMe == null){
    			throw new RuntimeException("Stoppable nicht gesetzt");
    		}
    		stoppMe.stop();
    		return;
    	}
    	Tile actTile = getActualTile();
    	TreeMap<Tile, Integer> TilesWithLengthForDestTile = getTilesWithLengthForDestTile(actTile);
    	Bag agentsInMoveRateDistance = getAgentsInMoveRateDistance(actTile);
    	//TODO: agentBag.isEmpty() ->  wenn agentsInMoveRateDistance = null, dann ist nur weglänge entscheidend 
    }

	private Bag getAgentsInMoveRateDistance(Tile actTile) {
		Bag agentBag = Room.agents.getObjectsWithinDistance(actTile, moveRate*Room.TILESIZE);
		
		
		return agentBag;
	}

	private TreeMap<Tile, Integer> getTilesWithLengthForDestTile(Tile actTile) {
		TreeMap<Tile, Integer> tmapWithTiles = new TreeMap<Tile, Integer>();
		Bag tilesInMoveRateDistance = roomState.allTilesOfMap.getObjectsWithinDistance(actTile, moveRate*Room.TILESIZE);
    	if (tilesInMoveRateDistance.isEmpty()){
    		LOGGER.error("Fehler: Tile {} hat keine NachbarTiles", actTile);
    		throw new RuntimeException("Tile hat kein Nachbarn");
    	}
    	for (Object o : tilesInMoveRateDistance){
    		Tile tile = (Tile) o;
    		if (tile.isUsable()){
    			int length = tile.getDestinations().get(destTile);
        		tmapWithTiles.put(tile, length);
    		}
    		
    	}
		return tmapWithTiles;
	}

	public Tile getActualTile() {
		return roomState.getTileByCoord(location.getX(), location.getY());
	}
    

    private boolean isTargetReached() {
		return destTile.equals(getActualTile());
	}
    
    
    /**
     * @return {@link ArrayList} CostTile - Elemente die außerhalb der tile-map liegen kommen nicht vor
     */
    public ArrayList<CostTile> getCostsForAgent(){
        ArrayList<CostTile> map = new ArrayList<CostTile>();
        Tile actTile = getActualTile();
        int ax = actTile.getX();
        int ay = actTile.getY();
        for (int x=-maxMoveRate; x < maxMoveRate+1; x++){
        	for (int y=-maxMoveRate; y < maxMoveRate+1; y++){
        		if (ax+x<0 || ay+y<0){
        			continue; //wenn das Tile an Position ax+x außerhalb der Tilemap ist, dann mach nix
        		}
        		if(x==0 && y==0){
        			CostTile ct = new CostTile(ax, ay, OWNCOST);
        			map.add(ct);
        			continue;
        		} 
        		Tile t = roomState.getTile(ax+x, ay+y);
        		//Berechnung von w nach gibbs-marskjös
        		Geometry actTilePoly = actTile.getGeometry();
        		Geometry targetTilePoly = t.getGeometry();
        		double distance = actTilePoly.getCentroid().distance(targetTilePoly.getCentroid());
        		BigDecimal dist = BigDecimal.valueOf(distance);
        		dist = dist.subtract(new BigDecimal("0.4"));
        		dist = dist.pow(2);
        		BigDecimal divisor = new BigDecimal("0.015");
        		divisor = divisor.add(dist);
        		BigDecimal w = new BigDecimal("1.0");
        		w = w.divide(divisor, RoundingMode.HALF_UP);
        		int costs = w.setScale(0, RoundingMode.HALF_UP).intValue();
        		CostTile costt = new CostTile(ax+x, ay+y, costs);
        		map.add(costt);
        	}
        }
        return map;
    }


	private void moveAgentOnPath(SimState state){
    	if (isTargetReachedOnPath()){
    		LOGGER.info("Agent {} hat Ziel erreicht",this);
    		if (stoppMe == null){
    			throw new RuntimeException("Stoppable nicht gesetzt");
    		}
    		stoppMe.stop();
    		return;
    	}
    	if(isAnyTileInViewDistanceBlocked()){
    		Tile start = pathAsTileList.get(step);
    		Tile end = pathAsTileList.get(pathAsTileList.size()-1);
    		Path p = roomState.calcNewPath(this, start, end);
    		if (p == null){
    			destinationChanger++;
    			return;
    		}
    		setPath(state, p);
    	}
    	Tile nextTile = getNextTileOnPath();
    	nextTile.addToPotentialList(this);
		getActTileOnPath().removeFromPotentialList(this);
    	Coordinate  coord = roomState.getCoordForTile(nextTile);
		moveTo(coord);
		step += moveRate;
    }

	private boolean isAnyTileInViewDistanceBlocked() {
		for (int i= step; i<step+maxMoveRate; i++){
			Tile t = pathAsTileList.get(i);
			if (roomState.isBlocked(this, t)){
				return true;
			}
		}
		return false;
	}

	private Tile getActTileOnPath() {
		return pathAsTileList.get(step);
	}

	private Tile getNextTileOnPath() {
		if (step+moveRate >= pathAsTileList.size()){
			return pathAsTileList.get(pathAsTileList.size()-1);
		}
		return pathAsTileList.get(step+moveRate);
	}

	private boolean isTargetReachedOnPath() {
		return step>=pathAsTileList.size()-1;
	}
    
	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(SimState state, Path p) {
		setStateDependingOnStadium(state);
		step = 0;
		if (p!=null){
			this.path = p;
			//packe alle tiles in eine arraylist zum einfachen arbeiten
			setPathAsTileList();
		}
	}
	
    // bewegt den Agenten zu den gegebenen Koordinaten
    public void moveTo(Coordinate c)
    {
        pointMoveTo.setCoordinate(c);
        location.apply(pointMoveTo);
    }
    
    private void setPathAsTileList(){
    	pathAsTileList = new ArrayList<Tile>(); //TODO: evtl .clear() machen, wenn ram voll läuft
    	for (int i=0; i< path.getLength();i++){
			Step step = path.getStep(i);
			Tile t = roomState.getTile(step.getX(), step.getY());
			pathAsTileList.add(i, t);
		}
    }
    
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
	
    public void setLocation(Point p){ 
    	location = p; 
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
	public int getViewDistance() {
		return maxMoveRate;
	}

	/**
	 * @param maxMoveRate the maxMoveRate to set
	 */
	public void setViewDistance(int viewDistance) {
		this.maxMoveRate = viewDistance;
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
