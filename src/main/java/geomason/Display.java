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

import com.vividsolutions.jts.geom.Geometry;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

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
	private Geometry dangerArea;

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
		dangerIndex = dangerInObservedTiles();
		if (dangerIndex >= 5 && dangerInfoSend == false) {
			informDisplays(true);
			dangerInfoSend = true;
		}
		if (dangerIndex <5 && dangerInfoSend == true){
			informDisplays(false);
			dangerInfoSend = false;
		}
		if (changeDestination){
			changeDestination(state);
		}
	}

	private void informDisplays(boolean b) {
		if (displayList.contains(this)){
			displayList.remove(this);
		}
		for (Display d : displayList){
			d.setChangeDestination(b, this.geometry);
			LOGGER.info("DANGER: {}",b);
		}		
	}

	private void changeDestination(SimState state) {
		if (dynamic){
			Tile newDest = null;
			int length = Integer.MAX_VALUE;
			for(Map.Entry<Tile, Path> entry : destinationList.entrySet()){
				Tile key = entry.getKey();
				Path val = entry.getValue();
				ArrayList<Tile> pathAsTileList = setPathAsTileList(state, val);
				for (Tile t : pathAsTileList){
					if (dangerArea.isWithinDistance(t.getGeometry().getCentroid(), Room.TILESIZE)){
						break;
					}
				}
				if (val.getLength()< length){
					length = val.getLength();
					newDest = key;
				}
			}
			if (newDest != null){
				changeDestinationForRandomAgents(state, newDest);
			}
		}
	}

	private void changeDestinationForRandomAgents(SimState state, Tile newDest) {
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
			a.setDestTile(newDest);
			tmpAgentlist.remove(index);
		}
		LOGGER.info("Dangerindex geändert für {} Agenten", agentsRecognized);
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
		dangerArea = g;
		this.changeDestination = changeDestination;
		LOGGER.info("DANGER ERHALTEN");
	}

}
