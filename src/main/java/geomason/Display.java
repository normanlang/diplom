package geomason;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class Display extends MasonGeometry implements Steppable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7269345727875001691L;
	private static final Logger LOGGER = LoggerFactory.getLogger(Display.class);
	private ArrayList<Display> displayList = new ArrayList<Display>();
	private Bag observingTiles = new Bag();
	private int dangerIndex = 0;
	private Stoppable stoppMe;
	private ArrayList<RoomAgent> agentlist = new ArrayList<RoomAgent>();
	private HashMap<Tile, Path> destinationList = new HashMap<Tile, Path>();
	private boolean dynamic;
	private boolean changeDestination = false;
	private boolean dangerInfoSend = false;
	private ArrayList<Geometry> dangerAreas = new ArrayList<Geometry>();

	/**
	 *  constructs the display
	 * @param observBag Bag with all Tiles which should be observed
	 * @param centerTile the Center-Tile of the observed polygon
	 */
	public Display(Bag observBag, boolean dynamic, HashMap<Tile, Path> destinationList) {
		observingTiles = observBag;
		this.destinationList = destinationList;
		this.dynamic = dynamic;
	}

	/* (non-Javadoc)
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	public void step(SimState state) {
		agentlist.clear();
		if (dynamic){
			dangerIndex = dangerInObservedTiles();
			if (dangerIndex >= 5 && dangerInfoSend == false) {
				addAdditionalCosts(state);
				informDisplays(state, true);
				dangerInfoSend = true;
			}
			if (dangerIndex <5 && dangerInfoSend == true){
				deleteAdditionalCosts(state);
				informDisplays(state, false);
				dangerInfoSend = false;
			}
			if (changeDestination){
				changeDestinationForRandomAgents(state);
			}
		}
	}

	private void informDisplays(SimState state, boolean b) {
		if (displayList.contains(this)){
			displayList.remove(this);
		}
		for (Display d : displayList){
			d.setChangeDestination(b, this.geometry);
		}
		
	}
	private void deleteAdditionalCosts(SimState state){
		for (Object o : observingTiles){
			Tile t = (Tile)o;
			if (t.isUsable()){
				t.setAddCosts(0);
			}
		}
	}
	private void addAdditionalCosts(SimState state) {
		for (Object o : observingTiles){
			Tile t = (Tile)o;
			if (t.isUsable() && t.getAddCosts()==0){
				t.setAddCosts(100 * dangerIndex);
			}
		}
	}

	private void changeDestinationForRandomAgents(SimState state) {
		Tile newDest = setNewDestination(state);
//		if (newDest != null && !(endTilesWithManipulatedCosts.contains(newDest)) ){
//			addCheaperCostsForNewDestinationPath(state, newDest);
//			endTilesWithManipulatedCosts.add(newDest);
//		}
		int percent = ((Room)state).possibility;
		ArrayList<RoomAgent> tmpAgentlist = new ArrayList<RoomAgent>();
		tmpAgentlist.addAll(agentlist);
		BigDecimal tmp = BigDecimal.valueOf(agentlist.size());
		tmp = tmp.multiply(BigDecimal.valueOf(percent));
		tmp = tmp.divide(new BigDecimal("100"), RoundingMode.HALF_UP);
		int agentsRecognized = tmp.setScale(0, RoundingMode.HALF_UP).intValue();
		for (int i = 0; i < agentsRecognized; i++){
			int index = state.random.nextInt(tmpAgentlist.size());
			RoomAgent a = tmpAgentlist.get(index);
			if (newDest != null){
				a.setDestTile(newDest);
			}
			a.setDisplayRecognized(true);
			tmpAgentlist.remove(index);
		}
		
	}

	private Tile addCheaperCostsForNewDestinationPath(SimState state, Tile newDest){
		//TODO: dafür sorgen, dass die nicht jeden step aufgerufen wird
		Room room = (Room) state;
		Point point = this.geometry.getCentroid();
		Tile start = room.getTileByCoord(point.getX(), point.getY());
		//fakeAgent
		RoomAgent a = new RoomAgent(); 
		Path p = room.calcNewPath(a, start, newDest);
		if (p != null){
			ArrayList<Tile> pathlist = setPathAsTileList(room, p);
			for (Tile t : pathlist){
				Bag neighbours = room.allTilesOfMap.getObjectsWithinDistance(t, Room.TILESIZE*2);
				neighbours.removeAll(pathlist);
				for (Object o : neighbours){
					Tile ti = (Tile) o;
					ti.setAddCosts(-150);
				}
				t.setAddCosts(-300);
			}
		}
		return null;
	}
	private Tile setNewDestination(SimState state){
		Tile newDest = null;
		int length = Integer.MAX_VALUE;
		for(Map.Entry<Tile, Path> entry : destinationList.entrySet()){
			Tile key = entry.getKey();
			Path val = entry.getValue();
			ArrayList<Tile> pathAsTileList = setPathAsTileList(state, val);
			boolean danger = false;
			for (Tile t : pathAsTileList){
				for (Geometry g : dangerAreas){
					if (g.isWithinDistance(t.getGeometry().getCentroid(), Room.TILESIZE)){
						danger =true;
						break;
					}	
				}
				
			}
			if (val.getLength()< length && danger == false){
				length = val.getLength();
				newDest = key;
			}
		}
		return newDest;
	}
	private ArrayList<Tile> setPathAsTileList(SimState state, Path path){
	    ArrayList<Tile> pathAsTileList = new ArrayList<Tile>(); //TODO: evtl .clear() machen, wenn ram voll läuft
	    for (int i=0; i< path.getLength();i++){
	    	Step step = path.getStep(i);
	    	Tile t = ((Room)state).getTile(step.getX(), step.getY());
	    	pathAsTileList.add(i, t);
		}
	    return pathAsTileList;
	}
	
	/**
	 * @return the danger index for the observed polygon
	 */
	private int dangerInObservedTiles() {

		for (Object o : observingTiles) {
			Tile t = (Tile) o;
			if (!(t.getPotentialAgentsList().isEmpty())) {
				agentlist.add(t.getPotentialAgentsList().get(0));
			}
		}
		double area = this.getGeometry().getArea();
		if (area == 0) {
			return 0;
		}
		BigDecimal tmp = BigDecimal.valueOf(agentlist.size());
		tmp = tmp.divide(BigDecimal.valueOf(area), 2, RoundingMode.HALF_UP);
		int dangerFromAgents = 0;
		int roundTmp = tmp.setScale(0, RoundingMode.CEILING).intValue();
		if (tmp.compareTo(BigDecimal.valueOf(3)) > 0) {

			dangerFromAgents = 1 + roundTmp;
		} else
			dangerFromAgents = roundTmp;
		int dangerFromEnvironment = calcDangerFromEnvironment();
		return Math.max(dangerFromAgents, dangerFromEnvironment);
	}

	private int calcDangerFromEnvironment() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @return the displayList
	 */
	public ArrayList<Display> getDisplayList() {
		return displayList;
	}

	/**
	 * @param displayList
	 *            the displayList to set
	 */
	public void setDisplayList(ArrayList<Display> displayList) {
		this.displayList = displayList;
	}

	/**
	 * @param stoppMe
	 *            the stoppMe to set
	 */
	public void setStoppMe(Stoppable stoppMe) {
		this.stoppMe = stoppMe;
	}

	/**
	 * stops this Steppable-object. It is removed from the schedule 
	 */
	public void stoppMe() {
		stoppMe.stop();
	}
	/**
	 * @param changeDestination the changeDestination to set
	 */
	public synchronized void setChangeDestination(boolean changeDestination, Geometry g) {
		dangerAreas.add(g);
		this.changeDestination = changeDestination;
//		LOGGER.info("DANGER ERHALTEN");
	}

}
