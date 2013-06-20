package geomason;

import java.util.ArrayList;

import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class Tile extends MasonGeometry{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5161567811618409640L;
	private ArrayList<Agent> agentList = new ArrayList<Agent>();
	private boolean usable = false;
	private Polygon polygon = null;
	
	public Tile() {
		super();
	}

	public void setPolygon(Polygon p){
		polygon = p;
	}
	
	public Geometry getGeometry(){
		return polygon;
	}
	
	public void setUsable(boolean u){
		usable = u;
	}
	
	
	public boolean isUsable(){
		return usable;
	}
	
	
	public void addAgent(Agent a){
		agentList.add(a);
		
	}
	
	public void addAgents(ArrayList<Agent> al){
		agentList.addAll(al);
	}
	public void removeAgent(Agent a){
		agentList.remove(a);
	}
	
	public void removeAgents(ArrayList<Agent> al){
		agentList.removeAll(al);
	}

	/**
	 * @return the agentList
	 */
	public ArrayList<Agent> getAgentList() {
		return agentList;
	}
	
	
}
