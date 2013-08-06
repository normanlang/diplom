package geomason;

import static geomason.TestRoomMap.STATIC_MAP_TILES_CSV;
import examples.TestRoom;
import examples.TestRoomSmall;
import geomason.RoomAgent.Stadium;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class Room extends SimState{

		private static final long serialVersionUID = -1430063512195387977L;
		private static final Logger LOGGER = LoggerFactory.getLogger(Room.class);
		public static final double TILESIZE = 0.5;
		public static final int WIDTH = 800; 
		public static final int HEIGHT = 600;
	    public int NUM_AGENTS;
	    public static GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);   
	    public GeomVectorField movingSpace = new GeomVectorField();
	    public GeomVectorField obstacles = new GeomVectorField();
	    public GeomVectorField destinations = new GeomVectorField();
	    public GeomVectorField starts = new GeomVectorField();
	    public GeomVectorField allTilesOfMap = new GeomVectorField();
		private Bag allTilesOfDestinations = new Bag();
		private Bag allTilesOfStarts = new Bag();
	    private TestRoomMap map;
	    private Bag allDestinationCenterTiles = new Bag();
	    private int maxMoveRate;
	    
		public Room(long seed) {
			super(seed);
//			loadTestRoomData();
			loadTestRoomSmallData();
	        map = new TestRoomMap(this);
	        getAllDestinationsAndStartsTiles();
	        getAllCenterTilesOfDestinations();
	        Stadium stadium = Stadium.TEST;
	        if (!(new File(stadium.name() + "-" + STATIC_MAP_TILES_CSV).exists())){
	        	LOGGER.trace("create static floor field");
	        	map.createStaticFloorField(allDestinationCenterTiles, stadium);
	        } else {
	        	map.readStaticFloorField(stadium);
	        }
	        System.out.println("");
		}
		
		private void loadTestRoomSmallData() {
			TestRoomSmall testroomSmall = new TestRoomSmall(WIDTH,HEIGHT);
			movingSpace = testroomSmall.getMovingSpace();
			obstacles = testroomSmall.getObstacles();
			destinations = testroomSmall.getDestinations();
			NUM_AGENTS = testroomSmall.getNUM_AGENTS();
			starts = testroomSmall.getStarts();
			maxMoveRate = testroomSmall.getMaxMoveRateInTiles();
		}
		private void loadTestRoomData() {
			TestRoom testroom = new TestRoom(WIDTH,HEIGHT);
			movingSpace = testroom.getMovingSpace();
			obstacles = testroom.getObstacles();
			destinations = testroom.getDestinations();
			NUM_AGENTS = TestRoom.getNUM_AGENTS();
			starts = testroom.getStarts();
			maxMoveRate = testroom.getViewDistanceInTiles();
		}

		private void addAgents(){
			   Bag tmpStarts = new Bag();
			   tmpStarts.addAll(allTilesOfStarts);
			   tmpStarts.shuffle(random);
			   Bag tmpDests = new Bag();
			   tmpDests.addAll(allDestinationCenterTiles);
		        for (int i = 0; i < NUM_AGENTS; i++){
		        	if (movingSpace.getGeometries().isEmpty()){
		        		throw new RuntimeException("No polygons found.");
		            }
		        	if (tmpStarts.isEmpty()){
		        		LOGGER.error("All start tiles filled. "+(NUM_AGENTS-i)+" agents left out");
		        		break;
		        	}
		        	Tile startTile = (Tile) tmpStarts.pop();
		        	Tile endTile = (Tile) tmpDests.get(random.nextInt(tmpDests.size()));
		        	RoomAgent a = new RoomAgent(i, Stadium.TEST, generateRandomMoveRate(), maxMoveRate, endTile);
		        	startTile.addToPotentialList(a);
		        	Path p = calcNewPath(a, startTile, endTile);
		        	if (p!=null){
		        		a.setPath(this, p);
		        	}
		        	Point loc = new GeometryFactory().createPoint(getCoordForTile(startTile));
			    	a.setLocation(loc);
			    	MasonGeometry mg = new MasonGeometry(a.getGeometry());
			    	mg.isMovable = true;
	    			agents.addGeometry(mg);
	                Stoppable stoppable = schedule.scheduleRepeating(a);
	                a.setStoppMe(stoppable);
	                ArrayList<CostTile> test= a.getCostsForAgent();
	                System.out.println("");
		        }        
		    }
		    //grund- und hilfsfunktionen
		

		private Bag getAllCenterTilesOfDestinations(){
			Bag tmp = new Bag(destinations.getGeometries());
			Bag dests  = new Bag();
			dests.addAll(tmp);
			while (!dests.isEmpty()){
				MasonGeometry mg = (MasonGeometry) dests.pop();
				Point p = mg.getGeometry().getCentroid();
				Tile t = getTileByCoord(p.getX(), p.getY());
				if (!allDestinationCenterTiles.contains(t)){
					allDestinationCenterTiles.add(t);
				}
			}
			return allDestinationCenterTiles;
		}

		private void getAllDestinationsAndStartsTiles() {
			int width = getWidthInTiles();
			int height = getHeightInTiles();
			for (int tx=0;tx< width; tx++){
				for (int ty=0;ty<height; ty++){
					Tile t = map.getTile(tx, ty);
					boolean empty = (destinations.getObjectsWithinDistance(t, TILESIZE)).isEmpty();
					if (!empty){
						if (!allTilesOfDestinations.contains(t)){
							allTilesOfDestinations.add(t);
						}
					}
					empty = (starts.getObjectsWithinDistance(t, TILESIZE)).isEmpty();
					if (!empty){
						if (!allTilesOfStarts.contains(t)){
							allTilesOfStarts.add(t);
						}
					}
				}
			}
		}

		public int getWidthInTiles(){
			Envelope mbr = movingSpace.getMBR();
			//stellt sicher dass die Länge der Fläche an tiles min. so gross ist wie die länge
			//ein Tile soll 0,5x0,5m betragen
			int widthinTiles = (int) (Math.ceil(mbr.getWidth()) / TILESIZE); 
			return widthinTiles;
		}
		public int getHeightInTiles(){
			Envelope mbr = movingSpace.getMBR();
			//stellt sicher dass die Breite der Fläche an tiles min. so gross ist wie die breite
			//ein Tile soll 0,5x0,5m betragen
			int heightInTiles = (int) (Math.ceil(mbr.getHeight()) / TILESIZE); 
			return heightInTiles;
		}
		
		
		/**
		 * gets the tile at position x,y
		 * @param x 
		 * @param y
		 * @return
		 */
		public Tile getTileByCoord(double x, double y){
			//da die tiles bei dem minX und minY des MBR anfangen, ist die Position 
			//(minX, minY) in der Map das Tile an der Stelle (0,0)
			int tileX, tileY;
			double tempX = x - Math.floor(movingSpace.getMBR().getMinX());
			double tempY = y - Math.floor(movingSpace.getMBR().getMinY());
			if (tempX - Math.floor(tempX) < TILESIZE){
				tileX = (int) ((int) Math.floor(tempX) / TILESIZE);
			} else {
				tileX = (int) ((int) Math.floor(tempX) / TILESIZE) +1;
			}
			if (tempY - Math.floor(tempY) < TILESIZE){
				tileY = (int) ((int) Math.floor(tempY) / TILESIZE);
			} else {
				tileY = (int) ((int) Math.floor(tempY) / TILESIZE) +1;
			}
			return map.getTile(tileX, tileY);
		}
		
		public Coordinate getCoordForTile(Tile tile){
			double posX = Math.floor(movingSpace.getMBR().getMinX()) + tile.getX() * TILESIZE;
			double posY = Math.floor(movingSpace.getMBR().getMinY()) + tile.getY() * TILESIZE;
			Coordinate coord = new Coordinate(posX, posY);
			return coord;
		}
		
		public Tile getTile(int x, int y){
			if (map.getTile(x, y) == null){
				System.out.println("index out of bounds - TestRoom - getTile");
			}
			return map.getTile(x, y);
		}	


		public Path calcNewPath(RoomAgent a, Tile start, Tile end){
			int actX = start.getX(), actY = start.getY(); 
			int destX = end.getX(), destY = end.getY();
	        int maxNodes = getWidthInTiles() * getHeightInTiles();
			PathFinder find = new AStarPathFinder(map, maxNodes, true);
			Path newPath = find.findPath(a,actX, actY, destX, destY); 
			return newPath; 
		}
		
		// Methoden für UI und main
		public int getNumAgents(){ 
	    	return NUM_AGENTS; 
	    }
	    
	    public void setNumAgents(int a){ 
	    	if (a > 0) NUM_AGENTS = a; 
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
	    
	    public static void main(String[] args){
	        doLoop(Room.class, args);
	        System.exit(0);
	    }

		/**
		 * @return the allTilesOfDestinations
		 */
		public Bag getAllTilesOfDestinations() {
			return allTilesOfDestinations;
		}

		/**
		 * @return the allDestinationCenterTiles
		 */
		public Bag getAllDestinationCenterTiles() {
			return allDestinationCenterTiles;
		}

		
		private int generateRandomMoveRate() {
			int diff = random.nextInt(maxMoveRate);
			return maxMoveRate - diff;
		}

		public boolean isBlocked(Mover mover, Tile t) {
			return map.isBlocked(mover, t.getX(), t.getY());
			
		}

		/**
		 * @return the maxMoveRate
		 */
		public int getMaxMoveRate() {
			return maxMoveRate;
		}


}
