package geomason;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Point;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class Display extends MasonGeometry implements Steppable{

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
    private HashMap<Tile, Integer> destinationList = new HashMap<Tile, Integer>();
	
	public Display(Bag observBag, Tile centerTile){
		observingTiles = observBag;
		destinationList = centerTile.getDestinations();

	}
	
	public void step(SimState state) {
		agentlist.clear();
		dangerIndex = dangerInObservedTiles();
		if (dangerIndex >= 5){
			changeDestinationForRandomAgent(state);
			
		}
	}

	private void changeDestinationForRandomAgent(SimState state) {
		//TODO wieviele sehen das display?
		// alle agenten im bereich nachzählen wie oft welches ziel vorkommt? das rausnehmen und das nächst kürzere nehmen?
		for (RoomAgent ra : agentlist){
			
		}
		
		RoomAgent a = agentlist.get(state.random.nextInt(agentlist.size()));
		Tile agentTile = a.getPositionAsTile(state);
//		LOGGER.info("Dangerindex geändert für Agent {}", a.getId());
	}

	private int dangerInObservedTiles() {

		for (Object o : observingTiles){
			Tile t = (Tile) o;
			if (!(t.getPotentialAgentsList().isEmpty())){
				agentlist.add(t.getPotentialAgentsList().get(0));
			}
		}
		double area = this.getGeometry().getArea();
		if (area == 0){
			return 0;
		}
		BigDecimal tmp = BigDecimal.valueOf(agentlist.size());
		tmp = tmp.divide(BigDecimal.valueOf(area), 2, RoundingMode.HALF_UP);
		int dangerFromAgents = 0;
		int roundTmp = tmp.setScale(0, RoundingMode.CEILING).intValue();
		if (tmp.compareTo(BigDecimal.valueOf(3)) > 0){
			
			dangerFromAgents = 1 + roundTmp;
		} else dangerFromAgents = roundTmp;
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
	 * @param displayList the displayList to set
	 */
	public void setDisplayList(ArrayList<Display> displayList) {
		this.displayList = displayList;
	}

	/**
	 * @param stoppMe the stoppMe to set
	 */
	public void setStoppMe(Stoppable stoppMe) {
		this.stoppMe = stoppMe;
	}
	
	public void stoppMe(){
		stoppMe.stop();
	}
	

}
