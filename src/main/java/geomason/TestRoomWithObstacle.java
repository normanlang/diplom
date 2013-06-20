package geomason;

import java.io.FileNotFoundException;
import java.net.URL;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.planargraph.Node;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import org.newdawn.pathexample.UnitMover;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;
import org.newdawn.slick.util.pathfinding.Path.Step;

public class TestRoomWithObstacle extends SimState{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1430063512195387977L;

	public static final int WIDTH = 800; 
	public static final int HEIGHT = 600;
	public static final double TILESIZE = 0.5;
    public static int NUM_AGENTS = 1;
    public GeomVectorField movingSpace = new GeomVectorField(WIDTH, HEIGHT);
    public static GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);
    private int[] intArray = new int[21];
    public Bag blocks = new Bag();
    public Bag restPolygons = new Bag();
    public ArrayList<Block> blockCapacities = new ArrayList<Block>();
    public static ArrayList<String> homeFansStartingPoints = new ArrayList<String>();
    private PathFinder finder;
    private Path path;
    private TestRoomMap map;
    private TreeMap<Double, Agent> agentDistMap = new TreeMap<Double, Agent>();
    public double dmax = 15.0;
    
	public TestRoomWithObstacle(long seed) {
		super(seed);
		
        blockCapacities.add(new Block("Block O", 250, true));
        homeFansStartingPoints.add("evakuierung");
        //Array für random erzeugen
        int l = -10;
        for (int k=0;k< intArray.length;k++){
        	intArray[k] = l;
        	l++;
        }
        loadData();
        fillBlocksWithAdditionalData(movingSpace.getGeometries());
        map = new TestRoomMap(this);
	}
	
	   private void addAgents(){
	        for (int i = 0; i < NUM_AGENTS; i++){
	        	if (movingSpace.getGeometries().isEmpty()){
	        		throw new RuntimeException("No polygons found.");
	            }
	        	Point p = ((MasonGeometryBlock)blocks.get(2)).getGeometry().getCentroid();
	        	Point p2 = ((MasonGeometryBlock)movingSpace.getGeometries().get(1)).getGeometry().getCentroid();
	        	Coordinate coord = p.getCoordinate();
	        	Bag test = map.testRoomMap.getContainingObjects(p);
	        	Bag test2 = map.testRoomMap.getCoveringObjects(p);
	        	Tile start = map.getTile(p.getCoordinate().X, p.getCoordinate().Y);
	        	Tile dest = map.getTile(p2.getCoordinate().X, p2.getCoordinate().Y);
	        	System.out.println(p.getCoordinate().X+" "+ coord.x+ " "+coord.X);
	            Agent a = new Agent(Agent.Stadium.TEST, dest);
	            finder = new AStarPathFinder(map, 500, true);
	            path = finder.findPath(a,p.getCoordinate().X, p.getCoordinate().Y, p2.getCoordinate().X, p2.getCoordinate().Y);
	            a.setPath(path);
	            start.addAgent(a);
	            System.out.println("steps: "+path.getLength());
	            //adde home und awayFans in den jeweiligen Startpolygonen  
	            fanToSimulation(a); 
	        }        
	    }

	    private void fanToSimulation(Agent a) {
	    	int index = -1;
	    	if (a.homeFan){
	    		index = homeFansBlockIndex(movingSpace.getGeometries());
	    		if (index != -1){
	            	MasonGeometryBlock mgblock =(MasonGeometryBlock) movingSpace.getGeometries().objs[index];
	            	MasonGeometry mg = createMGOfAgent(mgblock, a); 
	            	mg.addStringAttribute("fanbase", "home");
	    			agents.addGeometry(mg);
	                schedule.scheduleRepeating(a);
	            }
	        } 
		}
		private MasonGeometry createMGOfAgent(MasonGeometryBlock mgb, Agent a) {
	    	Point p = null;
	    	while (true){
	    		p = mgb.getGeometry().getCentroid();
	    		int rdX = random.nextInt(21);
				int rdY = random.nextInt(21);
				AffineTransformation translate = AffineTransformation.translationInstance(intArray[rdX], intArray[rdY]);
	            p.apply(translate);
	            boolean contains = mgb.getGeometry().contains(p);
	            if (contains) break; 
	    	}
	    	a.setLocation(p);
	    	MasonGeometry mg = new MasonGeometry(a.getGeometry());
	    	mg.addIntegerAttribute("weight", a.weight);
	    	mg.isMovable = true;
			return mg;
		}

		private void fillBlocksWithAdditionalData(Bag ap){
	    	Bag allpolygons = ap;
	        if (allpolygons.isEmpty()){
	    		throw new RuntimeException("No polygons found.");
	        } else{
	        	for (int i=0; i< allpolygons.numObjs;i++){
	        		MasonGeometryBlock block = (MasonGeometryBlock) allpolygons.get(i);
	        		String blockName;
	        		if (block.getStringAttribute("Art") == null){
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
	            }
	        }
	    	
	    }
	    
	    private int homeFansBlockIndex(Bag polygons){
	    	
	        for (int j=0; j<polygons.numObjs; j++){
	        	MasonGeometryBlock region = (MasonGeometryBlock) polygons.get(j);
	        	//überprüfe ob der aktuelle masongeometryblock im Attribut "Art" einen der homFanStartingPoints zu stehen hat
	    		boolean blockHomeFans = (homeFansStartingPoints.contains(region.getStringAttribute("Art")));
	    		System.out.println("blockHomeFans: "+blockHomeFans+", area: "+region.getGeometry().getArea()+" numAgents: "+region.numAgentsInGeometry());
	    		//wenn die geometrie des aktuellen mgb nicht leer ist und noch nicht mehr als x Agenten in dieser sind und 
	    		//dieser block für homeFans gedacht ist, dann gib den aktuellen index zurück
	    		if (blockHomeFans && region.getGeometry().getArea() > 0.0 &&region.numAgentsInGeometry()<50){
	    			return j;
	    		}
	        }
	    	return -1;
	    }
	  

	    public void calculateLineOfSight(Agent a){
	    	Path p = a.getPath();
	    	
	    	Tile actualTile = this.getTile(a.getGeometry().getCoordinate().X, a.getGeometry().getCoordinate().Y);
	    	Bag agentsInDistance = agents.getObjectsWithinDistance(actualTile, dmax);
	    	Bag futurePosOfAgents = calcNextPosOfAgents(agentsInDistance);
	    	for (int i=0; i< futurePosOfAgents.numObjs; i++){
	    		double dist = a.getGeometry().distance(((Agent) futurePosOfAgents.get(i)).getGeometry());
	    		agentDistMap.put(new Double(dist), a);
	    	}
	    	System.out.println("");
	    	
	    	
	    }
	    
	    
	    
	    //grund- und hilfsfunktionen
	    
	    private Bag calcNextPosOfAgents(Bag agentsInDistance) {
	    	Bag agentsNextPos = null;
	    	if (agentsInDistance.isEmpty()){
	    		return agentsNextPos = new Bag();
	    	} else {
	    		for (int n=0;n<agentsInDistance.numObjs;n++){
		    		Agent ag = (Agent) agentsInDistance.get(n);
		    		//TODO: annahme nächste position wird erreicht ohne Probleme
		    		ag.calculateNextPosition();
		    		agentsNextPos = new Bag();
		    		agentsNextPos.add(ag);
		    	}
	    		return agentsNextPos;
	    	}
		}

		private void loadData(){
        // url für die vektordaten der Zonen und des Bewegungsraums
		URL testRoomBoundaries = TestRoomWithObstacle.class.getResource("data/movingSpace-testroom.shp");
        Bag movingSpaceAttributes = new Bag();
        movingSpaceAttributes.add("Art");     
        //lese vom Vektorlayer noch Attribute aus der shp-Datei aus

		try{
	        //lese den vector-layer des Raums und der Zonen ein
	        System.out.println("lese die Vektordaten ein...");
            ShapeFileImporter.read(testRoomBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryBlock.class);
        } catch (FileNotFoundException ex){
            System.out.println("ShapeFile import failed");
        }
		//sicher stellen, dass beide das gleiche minimum bounding rectangle(mbr) haben
		Envelope MBR = movingSpace.getMBR();
		movingSpace.computeConvexHull();
	}
	
	@Override
    public void start(){
        super.start();
        //entferne eventuell noch vorhandene Agenten
        agents.clear(); 
        //füge neue Agenten hinzu
        addAgents();
        //setze den minimum bounding rectangle anhand des Bewegungsraums
        agents.setMBR(movingSpace.getMBR());
    }
    
	public int getWidthinTiles(){
		Envelope mbr = movingSpace.getMBR();
		//stellt sicher dass die Länge der Fläche an tiles min. so gross ist wie die länge
		//ein Tile soll max 0,5x0,5m betragen
		int widthinTiles = (int) Math.ceil(mbr.getWidth() * TILESIZE); 
		return widthinTiles;
	}
	public int getHeightinTiles(){
		Envelope mbr = movingSpace.getMBR();
		//stellt sicher dass die Breite der Fläche an tiles min. so gross ist wie die breite
		//ein Tile soll max 0,5x0,5m betragen
		int heightInTiles = (int) Math.ceil(mbr.getHeight() * TILESIZE); 
		return heightInTiles;
	}
	
	public Tile getTile(int x, int y){
		if (x > -1 && x <= map.getWidthInTiles() && y > -1 && y <= map.getHeightInTiles()){
			return map.getTile(x, y);
		} else{
			System.out.println("getTile is out of bounds");
			return null;
		}
		
		
	}
	
	public Path calcNewPath(Agent a,int actX, int actY, int destX, int destY){
		PathFinder find = new AStarPathFinder(map, 500, true);
        return find.findPath(a,actX, actY, destX, destY);
	}
	
	// Methoden für UI und main
	public int getNumAgents(){ 
    	return NUM_AGENTS; 
    }
    
    public void setNumAgents(int a){ 
    	if (a > 0) NUM_AGENTS = a; 
    }
    
    
    public static void main(String[] args){
        doLoop(TestRoomWithObstacle.class, args);
        System.exit(0);
    }
}
