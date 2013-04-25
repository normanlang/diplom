package geomason;


import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class MasonGeometryWithAgentCounter extends MasonGeometry{
    private static final long serialVersionUID = 3186655744206152969L;


    public MasonGeometryWithAgentCounter(){
        super();
    }


    public int numAgentsInGeometry(){
        Bag coveredAgents = Stadium.agents.getCoveredObjects(this);
        return coveredAgents.numObjs;
    }
}
