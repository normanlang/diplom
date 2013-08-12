package geomason;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

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

	private ArrayList<Display> displayList = new ArrayList<Display>();
	private Bag observingTiles = new Bag();
	private int dangerIndex = 0;
    private Stoppable stoppMe;
	
	public Display(Bag observBag){
		observingTiles = observBag;

	}
	
	public void step(SimState state) {
		// TODO Auto-generated method stub
		dangerIndex = dangerInObservedTiles();
	}

	private int dangerInObservedTiles() {
		int agents = 0;
		for (Object o : observingTiles){
			Tile t = (Tile) o;
			if (!(t.getPotentialAgentsList().isEmpty())){
				agents++;
			}
		}
		int tiles = observingTiles.size();
		if (tiles == 0){
			return 0;
		}
		BigDecimal tmp = BigDecimal.valueOf(agents);
		tmp = tmp.divide(BigDecimal.valueOf(tiles), 2, RoundingMode.HALF_UP);
		tmp = tmp.multiply(BigDecimal.TEN);
		int dangerFromAgents = tmp.setScale(0, RoundingMode.HALF_UP).intValue();
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
