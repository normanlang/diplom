package geomason;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import java.io.FileNotFoundException;
import java.net.URL;

public class Stadium extends SimState{
	
    private static final long serialVersionUID = -2568637684893865458L;

	public static final int WIDTH = 600; 
	public static final int HEIGHT = 600; 
    public static int NUM_AGENTS = 120;

    public GeomVectorField movingSpace = new GeomVectorField(WIDTH, HEIGHT);
    public static GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);

    public Stadium(long seed){
        super(seed);

        // this line allows us to replace the standard MasonGeometry with our
        // own subclass of MasonGeometry; see CountingGeomWrapper.java for more info.
        // Note: this line MUST occur prior to ingesting the data
        URL movingSpaceBoundaries = Stadium.class.getResource("data/movingspace.shp");

        Bag empty = new Bag();
        try{
            ShapeFileImporter.read(movingSpaceBoundaries, movingSpace, empty, MasonGeometryWithAgentCounter.class);
        } catch (FileNotFoundException ex){
            System.out.println("ShapeFile import failed");
        }
        movingSpace.computeConvexHull();
       // county.computeUnion();
    }

    private void addAgents(){
        for (int i = 0; i < NUM_AGENTS; i++){
        	// zuf�lliger startpunkt
        	Bag allPolygons = movingSpace.getGeometries();
        	if (allPolygons.isEmpty()){
        		throw new RuntimeException("No polygons found.");
            }
        	MasonGeometry region = ((MasonGeometry)allPolygons.objs[random.nextInt(allPolygons.numObjs)]);
            // zufallsrichtung f�r die ausgangsbewegung
            Agent a = new Agent(random.nextInt(8));
            // setze jeden agenten ins zentrum eines Polygons
            a.setLocation(region.getGeometry().getCentroid());
            // pack die agenten ins GeomVectorField
            agents.addGeometry(new MasonGeometry(a.getGeometry()));
            schedule.scheduleRepeating(a);
            }        
    }
    @Override
    public void start(){
        super.start();
        agents.clear(); 
        addAgents();
        agents.setMBR(movingSpace.getMBR());
    }
    public int getNumAgents(){ 
    	return NUM_AGENTS; 
    }
    
    public void setNumAgents(int a){ 
    	if (a > 0) NUM_AGENTS = a; 
    }
    
    public static void main(String[] args){
        doLoop(Stadium.class, args);
        System.exit(0);
    }
}
