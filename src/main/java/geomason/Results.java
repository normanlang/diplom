package geomason;

import geomason.RoomAgent.Stadium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	private int deadAgents = 0;
	private Stadium stadium;
	private ArrayList<Display> displayList = new ArrayList<Display>();
	private HashMap<RoomAgent, Integer> pressureList = new HashMap<RoomAgent, Integer>();
	
	public Results(int numAgents, Stadium stadium){
		this.numAgents = numAgents;
		this.stadium = stadium;
	}
	public void step(SimState state) {
		schedule = state.schedule;
		String steps = String.valueOf(schedule.getSteps());
		if (schedule.getSteps()%100 == 0){
			LOGGER.info("Instance:{}, STEP:{}, AGENTEN GERETTET:{}, AGENTEN TOT:{}",
					Long.toString(state.seed()),
					steps,
					((Room) state).getNumAgents() - numAgents,
					deadAgents);
		}
		if (schedule.getSteps() > 10){
			if (!(pressureList.isEmpty())){
				for (Map.Entry<RoomAgent, Integer> entry : pressureList.entrySet()) {
				    RoomAgent key = entry.getKey();
				    int value = entry.getValue();
				    if (value >= 5*key.getMaxMoveRate()){
				    	LOGGER.info("Instance:{}, Agent {} TOT (Step: {})",
				    			Long.toString(state.seed()),
				    			key.getId(),
				    			schedule.getSteps());
				    	deadAgents++;
				    	key.stopMeNow();
				    }
				}
			}
		}
		
		if (numAgents == 0){
			for (Display d : displayList){
				d.stoppMe();
			}
			LOGGER.info("Instance:{}, ENDE;STEPS:{}, TOTE:{}",Long.toString(state.seed()), steps, deadAgents);
			stoppMe.stop();
			state.kill();
		}
		pressureList.clear();
		switch (stadium) {
		case TEST:
			if (schedule.getSteps() >= 1000){
				LOGGER.info("Instance:{}, ABBRUCH;STEPS:{}, TOTE:{}, Deadlock Agents: {}",Long.toString(state.seed()), steps, deadAgents, numAgents);
				state.kill();
			}
			break;
		case PREUSSEN:
			if (schedule.getSteps() >= 4500){
				LOGGER.info("Instance:{}, ABBRUCH;STEPS:{}, TOTE:{}, Deadlock Agents: {}",Long.toString(state.seed()), steps, deadAgents, numAgents);
				state.kill();
			}
		default:
			break;
		}
	}
	public synchronized void addAgentToPressureList(RoomAgent a, int pressure){
		if (pressureList.containsKey(a)){
			pressureList.put(a, pressureList.get(a) + pressure);
		} else{
			pressureList.put(a, pressure);
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
	
	public void setNumAgents(int numAgents){
		this.numAgents = numAgents;
	}
}
