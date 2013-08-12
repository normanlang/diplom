package geomason;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

public class Results implements Steppable{
	

	private static final long serialVersionUID = -6858380489207275634L;
	private static final Logger LOGGER = LoggerFactory.getLogger(Results.class);
	private Stoppable stoppMe;
	private Schedule schedule;
	private int numAgents;
	private ArrayList<Display> displayList = new ArrayList<Display>();
	
	public Results(int numAgents){
		this.numAgents = numAgents; 
	}
	public void step(SimState state) {
		schedule = state.schedule;
		if (numAgents == 0){
			String steps = String.valueOf(schedule.getSteps());
			for (Display d : displayList){
				d.stoppMe();
			}
			LOGGER.info("ENDE;STEPS:{}",steps);
			stoppMe.stop();
		}
		
	}
	
	public synchronized void reduceAgents(){
		if (numAgents>0){
			numAgents--;
		}
	}
	/**
	 * @param stoppMe the stoppMe to set
	 */
	public void setStoppMe(Stoppable stoppMe) {
		this.stoppMe = stoppMe;
	}
	
	public void setDisplayList (ArrayList<Display> dispList){
		displayList = dispList;
	}
}
