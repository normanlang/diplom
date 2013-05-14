package geomason;


import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class MasonGeometryBlock extends MasonGeometry{
    private static final long serialVersionUID = 3186655744206152969L;
    
    private int capacity;
    private String name;

	public MasonGeometryBlock(){
        super();   
        capacity = -1;
    }


    public int numAgentsInGeometry(){
        Bag coveredAgents = PreussenStadiumModel.agents.getCoveredObjects(this);
        return coveredAgents.numObjs;
    }
    
    public boolean capacityReached(){
    	if (capacity != -1 && this.numAgentsInGeometry() <= capacity){
    		return false;
    	} else return true;
    }
    public int getCapacity() {
		return capacity;
	}


	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
}
