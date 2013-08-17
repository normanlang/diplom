package examples;

import geomason.MasonGeometryBlock;
import geomason.Room;

import java.io.FileNotFoundException;
import java.net.URL;

import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;

import com.vividsolutions.jts.geom.Envelope;

public class TestRoom implements RoomInterface{
	
	
    public static int NUM_AGENTS = 15;
    private int maxMoveRate = 4; //in Tiles 
    private int maxPatience = 15;
    private GeomVectorField movingSpace, obstacles, destinations, starts, displays;
    Envelope MBR;
    
    public TestRoom(int w, int h){
    	movingSpace = new GeomVectorField(w, h);
        obstacles = new GeomVectorField(w, h);
        destinations = new GeomVectorField(w, h);
        starts = new GeomVectorField(w, h);
        displays = new GeomVectorField(w, h);
    	loadData();
    }
    
	private void loadData(){
        // url für die vektordaten der Zonen und des Bewegungsraums
		URL testRoomBoundaries = Room.class.getResource("data/displayversuch/movingspace.shp");
		URL obstacleBoundaries = Room.class.getResource("data/displayversuch/hindernisse.shp");
		URL startBoundaries = Room.class.getResource("data/displayversuch/start.shp");
		URL destBoundaries = Room.class.getResource("data/displayversuch/ziel.shp");
		URL displayBoundaries = Room.class.getResource("data/displayversuch/displays.shp");
        Bag movingSpaceAttributes = new Bag();
        movingSpaceAttributes.add("Art");     
        //lese vom Vektorlayer noch Attribute aus der shp-Datei aus

		try{
	        //lese den vector-layer des Raums und der Zonen ein
			System.out.println("Setup Testroom");
	        System.out.println("lese die Vektordaten ein...");
            ShapeFileImporter.read(testRoomBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryBlock.class);
            ShapeFileImporter.read(obstacleBoundaries, obstacles);
            ShapeFileImporter.read(startBoundaries, starts);
            ShapeFileImporter.read(destBoundaries, destinations);
            ShapeFileImporter.read(displayBoundaries, displays);
        } catch (FileNotFoundException ex){
            System.out.println("ShapeFile import failed");
        }
		//sicher stellen, dass beide das gleiche minimum bounding rectangle(mbr) haben
		MBR = movingSpace.getMBR();
		obstacles.setMBR(MBR);
		starts.setMBR(MBR);
		destinations.setMBR(MBR);
		displays.setMBR(MBR);
		obstacles.computeConvexHull();
		movingSpace.computeConvexHull();
		starts.computeConvexHull();
		destinations.computeConvexHull();
		displays.computeConvexHull();
	}

	/**
	 * @return the nUM_AGENTS
	 */
	public int getNUM_AGENTS() {
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
	 * @return the maxMoveRate
	 */
	public int getMaxMoveRateInTiles() {
		return maxMoveRate;
	}

	/**
	 * @return the maxPatience
	 */
	public int getMaxPatience() {
		return maxPatience;
	}

	public GeomVectorField getDisplays() {
		return displays;
	}


}
