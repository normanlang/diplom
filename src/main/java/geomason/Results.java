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

/**
 * @author Norman Langner
 * This class logs informations at certain steps and kills agents, when their pressure exceeds 5-times their max move rate
 */
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
	
	/**
	 * The constructor needs the information, how many agents are in the simulation and which Stadium is used
	 * @param numAgents
	 * @param stadium
	 */
	public Results(int numAgents, Stadium stadium){
		this.numAgents = numAgents;
		this.stadium = stadium;
	}
	/* (non-Javadoc)
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	public void step(SimState state) {
		schedule = state.schedule;
		String steps = String.valueOf(schedule.getSteps());
		//log every 100 steps or if only 10 agents are left in the simulations
		if (schedule.getSteps()%100 == 0 || (numAgents < 10 && numAgents > 0)){
			LOGGER.info("Instance:{}, STEP:{}, AGENTEN GERETTET:{}, AGENTEN TOT:{}",
					Long.toString(state.seed()),
					steps,
					((Room) state).getNumAgents() - numAgents,
					deadAgents);
		}
		//kill agents who are under too much pressure, but begin after 10 steps (because the
		//scenario is the worst case) 
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
		//remove me after schedule is empty of agents
		if (numAgents == 0){
			for (Display d : displayList){
				d.stoppMe();
			}
			LOGGER.info("Instance:{}, ENDE;STEPS:{}, TOTE:{}",Long.toString(state.seed()), steps, deadAgents);
			stoppMe.stop();
			state.kill();
		}
		pressureList.clear();
		//prevent that the schedule runs infinitly
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
	/**
	 * Adds an agent to the pressure list. If this agent is already in it, raise his pressure 
	 * @param a an agent
	 * @param pressure the pressure which is added to the agent
	 */
	public synchronized void addAgentToPressureList(RoomAgent a, int pressure){
		if (pressureList.containsKey(a)){
			pressureList.put(a, pressureList.get(a) + pressure);
		} else{
			pressureList.put(a, pressure);
		}
		
	}
	/**
	 *  Reduces the agent counter for the simulation
	 */
	public synchronized void reduceAgents(){
		if (numAgents>0){
			numAgents--;
		}
	}
	/**
	 * Like agents the simulation has to stop this observer when the simulation is over, otherwise the simulation runs infinitly
	 * @param stoppMe the stoppMe to set
	 */
	public void setStoppMe(Stoppable stoppMe) {
		this.stoppMe = stoppMe;
	}
	/**
	 * sets the Displaylist 
	 * @param dispList
	 */
	public void setDisplayList (ArrayList<Display> dispList){
		displayList = dispList;
	}
	/**
	 * Sets the number of agents in the simulation
	 * @param numAgents
	 */
	public void setNumAgents(int numAgents){
		this.numAgents = numAgents;
	}
}
