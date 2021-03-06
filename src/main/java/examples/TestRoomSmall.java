package examples;

import geomason.MasonGeometryBlock;
import geomason.Room;

import java.io.FileNotFoundException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;

import com.vividsolutions.jts.geom.Envelope;

public class TestRoomSmall implements RoomInterface{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestRoomSmall.class);
    public final int NUM_AGENTS = 5000;
    private int maxMoveRate = 5; //in Tiles 
    private int maxPatience = 15;
    private GeomVectorField movingSpace, obstacles, destinations, starts, displays;
    Envelope MBR;
    
    public TestRoomSmall(int w, int h){
    	movingSpace = new GeomVectorField(w, h);
        obstacles = new GeomVectorField(w, h);
        destinations = new GeomVectorField(w, h);
        starts = new GeomVectorField(w, h);
        displays = new GeomVectorField(w, h);
    	loadData();
    	addStartsAndDestinations();
    }
    
	private void loadData(){
        // url für die vektordaten der Zonen und des Bewegungsraums
		URL testRoomBoundaries = Room.class.getResource("data/testsmall/bewegungsraum.shp");
		URL obstacleBoundaries = Room.class.getResource("data/testsmall/hindernisseklein.shp");
		URL displaysBoundaries = Room.class.getResource("data/testsmall/displays.shp");
        Bag movingSpaceAttributes = new Bag();
        movingSpaceAttributes.add("Art");     
        Bag displayAttributes = new Bag();
        displayAttributes.add("DisplayID");
        //lese vom Vektorlayer noch Attribute aus der shp-Datei aus

		try{
	        //lese den vector-layer des Raums und der Zonen ein
			LOGGER.info("Setup Testroom");
			LOGGER.info("lese die Vektordaten ein...");
            ShapeFileImporter.read(testRoomBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryBlock.class);
            ShapeFileImporter.read(obstacleBoundaries, obstacles);
            ShapeFileImporter.read(displaysBoundaries, displays, displayAttributes);
        } catch (FileNotFoundException ex){
        	LOGGER.error("ShapeFile import failed");
        }
		//sicher stellen, dass beide das gleiche minimum bounding rectangle(mbr) haben
		MBR = movingSpace.getMBR();
		obstacles.setMBR(MBR);
		displays.setMBR(MBR);
		obstacles.computeConvexHull();
		movingSpace.computeConvexHull();
		displays.computeConvexHull();
	}
	
	private void addStartsAndDestinations() {
		Bag tmp = movingSpace.getGeometries();
		Bag allMGBs = new Bag();
		allMGBs.addAll(tmp);
		while (!allMGBs.isEmpty()){
			MasonGeometryBlock mgb = (MasonGeometryBlock) allMGBs.pop();
			String name =  mgb.getStringAttribute("Art");
			if (name.equalsIgnoreCase("ziel")) {
				destinations.addGeometry(mgb);
			}
			if (name.equalsIgnoreCase("start1") || name.equalsIgnoreCase("start2")) {
				starts.addGeometry(mgb);
			}
		}
		
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
