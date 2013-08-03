package examples;

import geomason.MasonGeometryBlock;
import geomason.TestRoomWithObstacle;

import java.io.FileNotFoundException;
import java.net.URL;

import com.vividsolutions.jts.geom.Envelope;

import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class TestRoom {
	
	
    public static int NUM_AGENTS = 150;
    private int viewDistanceInTiles = 30; //in Tiles 
    private GeomVectorField movingSpace, obstacles, destinations, starts;
    Envelope MBR;
    
    public TestRoom(int w, int h){
    	movingSpace = new GeomVectorField(w, h);
        obstacles = new GeomVectorField(w, h);
        destinations = new GeomVectorField(w, h);
        starts = new GeomVectorField(w, h);
    	loadData();
    	addStartsAndDestinations();
    }
    
	private void loadData(){
        // url für die vektordaten der Zonen und des Bewegungsraums
		URL testRoomBoundaries = TestRoomWithObstacle.class.getResource("data/movingSpace-testroom.shp");
		URL obstacleBoundaries = TestRoomWithObstacle.class.getResource("data/hindernisse.shp");
        Bag movingSpaceAttributes = new Bag();
        movingSpaceAttributes.add("Art");     
        //lese vom Vektorlayer noch Attribute aus der shp-Datei aus

		try{
	        //lese den vector-layer des Raums und der Zonen ein
			System.out.println("Setup Testroom");
	        System.out.println("lese die Vektordaten ein...");
            ShapeFileImporter.read(testRoomBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryBlock.class);
            ShapeFileImporter.read(obstacleBoundaries, obstacles);
        } catch (FileNotFoundException ex){
            System.out.println("ShapeFile import failed");
        }
		//sicher stellen, dass beide das gleiche minimum bounding rectangle(mbr) haben
		MBR = movingSpace.getMBR();
		obstacles.setMBR(MBR);
		obstacles.computeConvexHull();
		movingSpace.computeConvexHull();
	}
	
	private void addStartsAndDestinations() {
		Bag tmp = movingSpace.getGeometries();
		Bag allMGBs = new Bag();
		allMGBs.addAll(tmp);
		while (!allMGBs.isEmpty()){
			MasonGeometryBlock mgb = (MasonGeometryBlock) allMGBs.pop();
			String name =  mgb.getStringAttribute("Art");
			if (name.equalsIgnoreCase("evakuierung")) {
				destinations.addGeometry(mgb);
			}
			if (name.equalsIgnoreCase("Block O")) {
				starts.addGeometry(mgb);
			}
		}
		
	}

	/**
	 * @return the nUM_AGENTS
	 */
	public static int getNUM_AGENTS() {
		return NUM_AGENTS;
	}

	/**
	 * @return the movingSpace
	 */
	public GeomVectorField getMovingSpace() {
		return movingSpace;
	}

	/**
	 * @return the obstacles
	 */
	public GeomVectorField getObstacles() {
		return obstacles;
	}

	/**
	 * @return the destinations
	 */
	public GeomVectorField getDestinations() {
		return destinations;
	}

	/**
	 * @return the starts
	 */
	public GeomVectorField getStarts() {
		return starts;
	}

	/**
	 * @return the viewDistanceInTiles
	 */
	public int getViewDistanceInTiles() {
		return viewDistanceInTiles;
	}
}