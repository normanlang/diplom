package geomason;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class Stadium extends SimState{
	
    private static final long serialVersionUID = -2568637684893865458L;

	public static final int WIDTH = 600; 
	public static final int HEIGHT = 600; 
    public static int NUM_AGENTS = 120;
    public static int MAX_NUM_HOMEFANS = 30;
    public static int MAX_NUM_AWAYFANS = 50;

    public GeomVectorField movingSpace = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField homeFansBlocks = new GeomVectorField(WIDTH, HEIGHT);
    public static GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);
    public static ArrayList<String> homeFansStartingPoints = new ArrayList<String>();
    public static ArrayList<String> awayFansStartingPoints = new ArrayList<String>();

    public Stadium(long seed){
        super(seed);

        // this line allows us to replace the standard MasonGeometry with our
        // own subclass of MasonGeometry; see CountingGeomWrapper.java for more info.
        // Note: this line MUST occur prior to ingesting the data
        URL movingSpaceBoundaries = Stadium.class.getResource("data/movingspace.shp");
        Bag movingSpaceAttributes = new Bag();
        //movingSpaceAttributes.add("id");
        movingSpaceAttributes.add("Art");
        //movingSpaceAttributes.add("id_2");
        movingSpaceAttributes.add("Art_2");
        try{
            ShapeFileImporter.read(movingSpaceBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryWithAgentCounter.class);
        } catch (FileNotFoundException ex){
            System.out.println("ShapeFile import failed");
        }
        movingSpace.computeConvexHull();
       // county.computeUnion();
        
        homeFansStartingPoints.add("Evakuierungszone Haupteingang");
        awayFansStartingPoints.add("Evakuierungszone Gegengerade");//"Evakuierungszone Gaeste"
    }

    private void addAgents(){
        for (int i = 0; i < NUM_AGENTS; i++){
        	Bag allPolygons = movingSpace.getGeometries();
        	if (allPolygons.isEmpty()){
        		throw new RuntimeException("No polygons found.");
            }
            // zufallsrichtung fuer die ausgangsbewegung
            Agent a = new Agent(random.nextInt(8));
            
            //finde die Startzone der HomeFans;
            int objectIndex = homeFansBlockIndex(allPolygons);
            if (objectIndex != -1){
            	a.setLocation(((MasonGeometryWithAgentCounter) allPolygons.objs[objectIndex]).getGeometry().getInteriorPoint());
    			agents.addGeometry(new MasonGeometry(a.getGeometry()));
                schedule.scheduleRepeating(a);
                System.out.println("HomeFans: "+i);
            } else if (objectIndex == -1){
            	objectIndex = awayFansBlockIndex(allPolygons);
            	a.setLocation(((MasonGeometryWithAgentCounter) allPolygons.objs[objectIndex]).getGeometry().getInteriorPoint());
    			agents.addGeometry(new MasonGeometry(a.getGeometry()));
                schedule.scheduleRepeating(a);
            }
            
        }        
    }
    
    private int homeFansBlockIndex(Bag polygons){
        for (int j=0; j<polygons.numObjs; j++){
        	MasonGeometryWithAgentCounter region = (MasonGeometryWithAgentCounter) polygons.objs[j];
    		boolean blockHomeFans = (homeFansStartingPoints.contains(region.getStringAttribute("Art")) ||
    				homeFansStartingPoints.contains(region.getStringAttribute("Art_2")));
    		if (blockHomeFans && region.numAgentsInGeometry() < MAX_NUM_HOMEFANS){
    			System.out.println(region.numAgentsInGeometry() +"<"+ MAX_NUM_HOMEFANS);
    			return j;
    		}
        }
    	return -1;
    }
    
    private int awayFansBlockIndex(Bag polygons){
    	for (int j=0; j<polygons.numObjs; j++){
        	MasonGeometryWithAgentCounter region = (MasonGeometryWithAgentCounter) polygons.objs[j];
        	boolean blockAwayFans = (awayFansStartingPoints.contains(region.getStringAttribute("Art")) ||
    				awayFansStartingPoints.contains(region.getStringAttribute("Art_2")));
        	if (blockAwayFans && region.numAgentsInGeometry() < MAX_NUM_AWAYFANS){
    			return j;
    		}
        }
    	return -1;
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
