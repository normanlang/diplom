package geomason;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;


import com.vividsolutions.jts.geom.Point;

public class PreussenStadiumModel extends SimState{
	
    private static final long serialVersionUID = -2568637684893865458L;

	public static final int WIDTH = 600; 
	public static final int HEIGHT = 600; 
    public static int NUM_AGENTS = 120;

    public GeomVectorField movingSpace = new GeomVectorField(WIDTH, HEIGHT);
    //public GeomVectorField homeFansBlocks = new GeomVectorField(WIDTH, HEIGHT);
    public static GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);

    public static ArrayList<String> homeFansStartingPoints = new ArrayList<String>();
    public static ArrayList<String> awayFansStartingPoints = new ArrayList<String>();
    public static ArrayList<String> homeFansBlocks = new ArrayList<String>();
    
    public Bag blocks = new Bag();
    public Bag restPolygons = new Bag();
    public ArrayList<Block> blockCapacities = new ArrayList<Block>();

    public PreussenStadiumModel(long seed){
        super(seed);
        
        //lege Kapazitäten der einzelnen Blocks fest
        blockCapacities.add(new Block("Block O", 250, true));
        blockCapacities.add(new Block("Block N", 3000, true));
        blockCapacities.add(new Block("Block M", 3000, true));
        blockCapacities.add(new Block("Block L", 1000, true));
        blockCapacities.add(new Block("Block K", 1000, true));
        blockCapacities.add(new Block("Block J", 1000, true));
        
        //lege Startpunkt für die jeweiligen Fans fest
        homeFansStartingPoints.add("Evakuierungszone Haupteingang");
        awayFansStartingPoints.add("Evakuierungszone Gegengerade");//"Evakuierungszone Gaeste"
        URL movingSpaceBoundaries = PreussenStadiumModel.class.getResource("data/movingspace.shp");
        Bag movingSpaceAttributes = new Bag();
        //movingSpaceAttributes.add("id");
        movingSpaceAttributes.add("Art");
        //movingSpaceAttributes.add("id_2");
        movingSpaceAttributes.add("Art_2");
        try{
            ShapeFileImporter.read(movingSpaceBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryBlock.class);
        } catch (FileNotFoundException ex){
            System.out.println("ShapeFile import failed");
        }
        movingSpace.computeConvexHull();
        fillBlocksWithAdditionalData(movingSpace.getGeometries());
        	
        
       // county.computeUnion();
        

    }
    
    private void addAgents(){
        for (int i = 0; i < NUM_AGENTS; i++){
        	if (movingSpace.getGeometries().isEmpty()){
        		throw new RuntimeException("No polygons found.");
            }
            // zufallsrichtung fuer die ausgangsbewegung
            Agent a = new Agent(random.nextInt(8));     
            //finde die Startzone der HomeFans;
            int objectIndex = homeFansBlockIndex(movingSpace.getGeometries());
            if (objectIndex != -1){
            	MasonGeometryBlock mgb =(MasonGeometryBlock) movingSpace.getGeometries().objs[objectIndex];
            	Point p = mgb.getGeometry().getInteriorPoint();
            	a.setLocation(p);
            	MasonGeometry mg = new MasonGeometry(a.getGeometry());
            	mg.addIntegerAttribute("weight", a.weight);
    			agents.addGeometry(mg);
                schedule.scheduleRepeating(a);
                System.out.println("HomeFans: "+i);
            } else {
            	objectIndex = awayFansBlockIndex(movingSpace.getGeometries());
            	if (objectIndex != -1){
            		a.setLocation(((MasonGeometryBlock) movingSpace.getGeometries().objs[objectIndex]).getGeometry().getInteriorPoint());
                	MasonGeometry mg = new MasonGeometry(a.getGeometry());
                	mg.addIntegerAttribute("weight", a.weight);
        			agents.addGeometry(mg);
                    schedule.scheduleRepeating(a);
            	}
            	
            }
            
        }        
    }

    private void fillBlocksWithAdditionalData(Bag ap){
    	Bag allpolygons = ap;
        if (allpolygons.isEmpty()){
    		throw new RuntimeException("No polygons found.");
        } else{
        	for (int i=0; i< allpolygons.numObjs;i++){
        		MasonGeometryBlock block = (MasonGeometryBlock) allpolygons.get(i);
        		String blockName;
        		if (block.getStringAttribute("Art") == null && block.getStringAttribute("Art_2") == null ||
        				block.getStringAttribute("Art") != null && block.getStringAttribute("Art_2") != null){
        			restPolygons.add(block);
        		}
        		if (block.getStringAttribute("Art") != null){
        			blockName = block.getStringAttribute("Art");
        			block.setName(blockName);
        			for (Block b : blockCapacities){
        				boolean equal = b.getBlockName().equalsIgnoreCase(blockName); 
        				if (equal){
        					block.setCapacity(b.getCapacity());
        				}
        			}
        			blocks.add(block);
        		}
        		if (block.getStringAttribute("Art_2") != null){
        			blockName = block.getStringAttribute("Art_2");
        			block.setName(blockName);
        			for (Block b : blockCapacities){
        				boolean equal = b.getBlockName().equalsIgnoreCase(blockName); 
        				if (equal){
        					block.setCapacity(b.getCapacity());
        				}
        			}
        			blocks.add(block);
        		}
    			
            }
        }
    	
    }
    
    private int homeFansBlockIndex(Bag polygons){
        for (int j=0; j<polygons.numObjs; j++){
        	MasonGeometryBlock region = (MasonGeometryBlock) polygons.get(j);
    		boolean blockHomeFans = (homeFansStartingPoints.contains(region.getStringAttribute("Art")) ||
    				homeFansStartingPoints.contains(region.getStringAttribute("Art_2")));
    		if (blockHomeFans && region.numAgentsInGeometry()<50){
    			return j;
    		}
        }
    	return -1;
    }
    
    private int awayFansBlockIndex(Bag polygons){
    	for (int j=0; j<polygons.numObjs; j++){
        	MasonGeometryBlock region = (MasonGeometryBlock) polygons.get(j);
        	boolean blockAwayFans = (awayFansStartingPoints.contains(region.getStringAttribute("Art")) ||
    				awayFansStartingPoints.contains(region.getStringAttribute("Art_2")));
        	if (blockAwayFans && region.numAgentsInGeometry()<50){
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
        doLoop(PreussenStadiumModel.class, args);
        System.exit(0);
    }
}
