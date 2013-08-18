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

public class Preussenstadion implements RoomInterface{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Preussenstadion.class);
    public final int NUM_AGENTS = 12500;
    private int maxMoveRate = 5; //in Tiles 
    private int maxPatience = 15;
    private GeomVectorField movingSpace, obstacles, destinations, starts, displays;
    Envelope MBR;
    private int width, height;
    
    public Preussenstadion(int w, int h){
    	width = w;
    	height = h;
    	movingSpace = new GeomVectorField(w, h);
        obstacles = new GeomVectorField(w, h);
        destinations = new GeomVectorField(w, h);
        starts = new GeomVectorField(w, h);
        displays = new GeomVectorField(w, h);
    	loadData();
    }
    
	private void loadData(){
        // url für die vektordaten der Zonen und des Bewegungsraums
		URL testRoomBoundaries = Room.class.getResource("data/diplom/movingspace.shp");
		URL obstacleBoundaries = Room.class.getResource("data/diplom/hindernisse.shp");
		URL displaysBoundaries = Room.class.getResource("data/diplom/displays.shp");
		URL heimStartsBoundaries = Room.class.getResource("data/diplom/startzonen.shp");
		URL zielBoundaries = Room.class.getResource("data/diplom/zielzonen.shp");
        Bag attributes = new Bag();
        attributes.add("Art");     
        Bag polyAttributes = new Bag();
        polyAttributes.add("Art");
        polyAttributes.add("ID1");
        //lese vom Vektorlayer noch Attribute aus der shp-Datei aus
        GeomVectorField tmpStarts = new GeomVectorField(width, height);
        GeomVectorField tmpDests = new GeomVectorField(width, height);
		try{
	        //lese den vector-layer des Raums und der Zonen ein
			LOGGER.info("Setup Testroom");
			LOGGER.info("lese die Vektordaten ein...");
            ShapeFileImporter.read(testRoomBoundaries, movingSpace, attributes, MasonGeometryBlock.class);
            ShapeFileImporter.read(obstacleBoundaries, obstacles);
            ShapeFileImporter.read(displaysBoundaries, displays, attributes);
            ShapeFileImporter.read(heimStartsBoundaries, tmpStarts, polyAttributes, MasonGeometryBlock.class);
            ShapeFileImporter.read(zielBoundaries, tmpDests, polyAttributes, MasonGeometryBlock.class);
        } catch (FileNotFoundException ex){
        	LOGGER.error("ShapeFile import failed");
        }
		//sicher stellen, dass beide das gleiche minimum bounding rectangle(mbr) haben
		removeGaeste(tmpStarts, tmpDests);
		MBR = movingSpace.getMBR();
		obstacles.setMBR(MBR);
		displays.setMBR(MBR);
		starts.setMBR(MBR);
		destinations.setMBR(MBR);
		obstacles.computeConvexHull();
		movingSpace.computeConvexHull();
		displays.computeConvexHull();
		starts.computeConvexHull();
		destinations.computeConvexHull();
		
	}
	private void removeGaeste(GeomVectorField s, GeomVectorField d) {
		Bag tmp = s.getGeometries();
		Bag tmpz = d.getGeometries();
		Bag allMGBs = new Bag();
		allMGBs.addAll(tmp);
		while (!allMGBs.isEmpty()){
			MasonGeometryBlock mgb = (MasonGeometryBlock) allMGBs.pop();
			String name =  mgb.getStringAttribute("Art");
			if (name.equalsIgnoreCase("heim")) {
				starts.addGeometry(mgb);
			}
		}
		allMGBs.clear();
		allMGBs.addAll(tmpz);
		while (!allMGBs.isEmpty()){
			MasonGeometryBlock mgb = (MasonGeometryBlock) allMGBs.pop();
			String name =  mgb.getStringAttribute("Art");
			if (name.equalsIgnoreCase("heim")) {
				destinations.addGeometry(mgb);
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
