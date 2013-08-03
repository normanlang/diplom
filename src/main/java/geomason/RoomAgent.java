package geomason;


import java.util.ArrayList;
import java.util.SortedMap;
import java.util.logging.Logger;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomVectorField;
import sim.util.geo.PointMoveTo;


public class RoomAgent implements Steppable, Mover{
	
	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RoomAgent.class);

    private static final long serialVersionUID = -5318720825474063385L;

    public Point location = null; 
    public int moveRate;
    public static final int fakeAgentID = 999999;
    public static enum Stadium {PREUSSEN,TEST,ESPRIT};
    private Stadium stadium;
    private Path path;
    private Room roomState = null;
    private ArrayList<Tile>  pathAsTileList = new ArrayList<Tile>();
    private ArrayList<Coordinate> pathAsCoordList = new ArrayList<Coordinate>();
    GeomVectorField accessableArea = null;
    private PointMoveTo pointMoveTo = new PointMoveTo();
    private int step = 0;
    private int id;
    private Stoppable stoppMe;
    private int destinationChanger;
    private int viewDistance;
    
    public RoomAgent(int id, Stadium stadium, int moveRate, int viewDist){
       	this.stadium = stadium;
       	this.id = id;
       	this.moveRate = moveRate;
       	this.viewDistance = viewDist;
    }
		
    public void step(SimState state){
        //weiter gehts
    	setStateDependingOnStadium(state);
    	moveAgent(state);
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
    	Tile nextTile = getNextTile();
    	nextTile.addToPotentialList(this);
		getActTile().removeFromPotentialList(this);
    	Coordinate  coord = roomState.getCoordForTile(nextTile);
		moveTo(coord);
		step += moveRate;

    	
    	
//    	Tile actTile = pathAsTileList.get(step);
//
//    	if (actTile.getPotentialAgentsList().contains(this) == false){
//    		actTile.addToPotentialList(this);
//    	}
//    	Tile nextTile;
//    	if (step+moveRate>pathAsTileList.size()-1){
//    		nextTile = pathAsTileList.get(pathAsTileList.size()-1);
//    		step = pathAsTileList.size();
//    	} else {
//    		nextTile = pathAsTileList.get(step+moveRate);
//    	}
//    	Coordinate coord = roomState.getCoordForTile(nextTile);
//    	if (nextTile.getPotentialAgentsList().isEmpty()){
//    		moveTo(coord);
//        	step += moveRate;
//        	actTile.removeFromPotentialList(this);
//        	nextTile.addToPotentialList(this);
//    	} else{
//    		Path p = roomState.calcNewPath(this, actTile, pathAsTileList.get(pathAsTileList.size()-1));
//    		setPath(state, p);
//    		step = 0;
//    		if (step+moveRate>pathAsTileList.size()-1){
//        		nextTile = pathAsTileList.get(pathAsTileList.size()-1);
//        		step = pathAsTileList.size();
//        	} else nextTile = pathAsTileList.get(step+moveRate);
//        	Coordinate c = roomState.getCoordForTile(nextTile);
//        	if (nextTile.getPotentialAgentsList().isEmpty()){
//        		moveTo(c);
//            	step = step + moveRate;
//            	actTile.removeFromPotentialList(this);
//            	nextTile.addToPotentialList(this);
//        	}
//    	}
    }

	private boolean isAnyTileInViewDistanceBlocked() {
		for (int i= step; i<step+viewDistance; i++){
			Tile t = pathAsTileList.get(i);
			if (roomState.isBlocked(this, t)){
				return true;
			}
		}
		return false;
	}

	private Tile getActTile() {
		return pathAsTileList.get(step);
	}

	private Tile getNextTile() {
		if (step+moveRate >= pathAsTileList.size()){
			return pathAsTileList.get(pathAsTileList.size()-1);
		}
		return pathAsTileList.get(step+moveRate);
	}

	private boolean isTargetReached() {
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
	 * @return the viewDistance
	 */
	public int getViewDistance() {
		return viewDistance;
	}

	/**
	 * @param viewDistance the viewDistance to set
	 */
	public void setViewDistance(int viewDistance) {
		this.viewDistance = viewDistance;
	}
}
