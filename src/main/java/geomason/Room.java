package geomason;

import static geomason.RoomAgent.fakeAgentID;
import static geomason.RoomMap.STATIC_MAP_TILES_CSV;
import examples.Preussenstadion;
import examples.TestRoom;
import examples.TestRoomSmall;
import geomason.RoomAgent.Stadium;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class Room extends SimState{

		private static final long serialVersionUID = -1430063512195387977L;
		private static final Logger LOGGER = LoggerFactory.getLogger(Room.class);
		public static final int OWNCOST = 1000;
		public static final double TILESIZE = 0.5;
		public static final int WIDTH = 800; 
		public static final int HEIGHT = 600;
		public int possibility; 
	    public int numAgents;
	    public static GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);   
	    public GeomVectorField movingSpace = new GeomVectorField();
	    public GeomVectorField obstacles = new GeomVectorField();
	    public GeomVectorField destinations = new GeomVectorField();
	    public GeomVectorField starts = new GeomVectorField();
	    public GeomVectorField allTilesOfMap = new GeomVectorField();
	    public GeomVectorField displays = new GeomVectorField();
		private Bag allTilesOfDestinations = new Bag();
		private Bag allTilesOfStarts = new Bag();
		private Bag allTilesOfDisplays = new Bag();
	    private RoomMap map;
	    private Bag allDestinationCenterTiles = new Bag();
	    private int maxMoveRate;
	    private int maxPatience;
	    private Results results; 
	    private Bag standardCosts;
	    private Stadium stadium;
	    private boolean dynamic;
	    
		public Room(long seed) {

			super(seed); 
			// HIER EINSTELLUNGEN FUER DIE SIMULATION VORNEHMEN!!!!
			setStadium(stadium.TESTSMALL);
			dynamic = true;  //dynamisch vs. statisch
			possibility = 50; // Wahrscheinlichkeit, wieviele Agenten die Änderung des Displays mitbekommen 10, 25, 50
			// -----------------------------------
			LOGGER.info("dynamische Displays? {}, Wahrscheinlichkeit: {}", dynamic, possibility);
			LOGGER.info("Daten erfolgreich geladen");
	        map = new RoomMap(this);
	        LOGGER.info("Erzeuge alle Start- und Zielzellen");
	        getAllDestinationsAndStartsTiles();
	        LOGGER.info("Start- und Zielzellen erfolgreich erzeugt");
	        getAllCenterTilesOfDestinations();
	        LOGGER.info("Erzeuge alle Displayzellen");
	        getAllTilesOfDisplays();
	        LOGGER.info("Displayzellen erfolgreich erzeugt");
	        Tile randomTile = (Tile) allDestinationCenterTiles.get(0);
	        standardCosts = calcCostsWithoutInfluences(randomTile, OWNCOST);
	        standardCostsCheck();
	        if (!(new File(stadium.name() + "-" + STATIC_MAP_TILES_CSV).exists())){
	        	LOGGER.trace("create static floor field");
	        	map.createStaticFloorField(allDestinationCenterTiles, stadium);
	        } else {
	        	map.readStaticFloorField(stadium);
	        }
		}
		private boolean standardCostsCheck() {
			if (maxMoveRate == 5){
				CostTile t1 = (CostTile) standardCosts.get(50);
				CostTile t2 = (CostTile) standardCosts.get(72);
				CostTile t3 = (CostTile) standardCosts.get(48);
				CostTile t4 = (CostTile) standardCosts.get(70);
				CostTile t5 = (CostTile) standardCosts.get(61);
				CostTile t6 = (CostTile) standardCosts.get(49);
				CostTile t7 = (CostTile) standardCosts.get(59);
				CostTile t8 = (CostTile) standardCosts.get(71);
		        if (!(t1.getCosts() == t2.getCosts() && t1.getCosts() == t3.getCosts() && t1.getCosts() == t4.getCosts())){
		        	LOGGER.debug("Standardkosten stimmen nicht");
		        	return false;
		        }
		        if (!(t5.getCosts() == t6.getCosts() && t5.getCosts() == t7.getCosts() && t5.getCosts() == t8.getCosts())){
		        	LOGGER.debug("Standardkosten stimmen nicht");
		        	return false;
		        }
			}
	        return true;
		}
		private void setStadium(Stadium stadium){
			this.stadium = stadium;
			switch (this.stadium){
		    	case PREUSSEN: //für Preussenstadion
		    		loadPreussenData();
		            break;
		    	case TEST:  
		    		loadTestRoomData();
		    		break;
		    	case TESTSMALL:
		    		loadTestRoomSmallData();
		    		break;
		    	default:
		    		loadTestRoomSmallData();
		    		break;
			}
	    		
		}
		
		private void loadPreussenData() {
			Preussenstadion preussen = new Preussenstadion(WIDTH,HEIGHT);
			movingSpace = preussen.getMovingSpace();
			obstacles = preussen.getObstacles();
			destinations = preussen.getDestinations();
			numAgents = preussen.getNUM_AGENTS();
			starts = preussen.getStarts();
			maxMoveRate = preussen.getMaxMoveRateInTiles();
			maxPatience = preussen.getMaxPatience();
			displays = preussen.getDisplays();
		}
		
		private void loadTestRoomSmallData() {
			TestRoomSmall testroomSmall = new TestRoomSmall(WIDTH,HEIGHT);
			movingSpace = testroomSmall.getMovingSpace();
			obstacles = testroomSmall.getObstacles();
			destinations = testroomSmall.getDestinations();
			numAgents = testroomSmall.getNUM_AGENTS();
			starts = testroomSmall.getStarts();
			maxMoveRate = testroomSmall.getMaxMoveRateInTiles();
			maxPatience = testroomSmall.getMaxPatience();
			displays = testroomSmall.getDisplays();
		}
		private void loadTestRoomData() {
			TestRoom testroom = new TestRoom(WIDTH,HEIGHT);
			movingSpace = testroom.getMovingSpace();
			obstacles = testroom.getObstacles();
			destinations = testroom.getDestinations();
			numAgents = testroom.getNUM_AGENTS();
			starts = testroom.getStarts();
			maxMoveRate = testroom.getMaxMoveRateInTiles();
			maxPatience = testroom.getMaxPatience();
			displays = testroom.getDisplays();
		}

		private void addAgents(){
			   Bag tmpStarts = new Bag();
			   tmpStarts.addAll(allTilesOfStarts);
			   tmpStarts.shuffle(random);
			   Bag tmpDests = new Bag();
			   tmpDests.addAll(allDestinationCenterTiles);
		        for (int i = 0; i < numAgents; i++){
		        	if (movingSpace.getGeometries().isEmpty()){
		        		throw new RuntimeException("No polygons found.");
		            }
		        	if (tmpStarts.isEmpty()){
		        		LOGGER.error("All start tiles filled. "+(numAgents-i)+" agents left out");
		        		results.setNumAgents(i);
		        		break;
		        	}
		        	Tile startTile = (Tile) tmpStarts.pop();
		        	Bag startZone = starts.getObjectsWithinDistance(startTile, TILESIZE);
		        	if (startZone.isEmpty()){
		        		LOGGER.error("Startzone != 1.");
		        		throw new RuntimeException("startZone != 1");
		        	}
		        	Tile endTile = getEndTileforStartTile(startZone, tmpDests);
		        	RoomAgent a = new RoomAgent(i, generateRandomMoveRate(), maxMoveRate, maxPatience, endTile, results);
		        	startTile.addToPotentialList(a);
		        	Point loc = new GeometryFactory().createPoint(getCoordForTile(startTile));
			    	a.setLocation(loc);
			    	a.isMovable = true;
	    			agents.addGeometry(a);
	                Stoppable stoppable = schedule.scheduleRepeating(a);
	                a.setStoppMe(stoppable);
		        }        
		    }
		    //grund- und hilfsfunktionen
		

		private Tile getEndTileforStartTile(Bag startZone, Bag tmpDests) {
			MasonGeometry mg = (MasonGeometry) startZone.get(0);
			boolean containsID1 = mg.getAttributes().containsKey("ID1");
			if (containsID1){
				int dest = mg.getIntegerAttribute("ID1");
				for (Object o : tmpDests){
					Tile t = (Tile) o;
					Bag endZones = destinations.getObjectsWithinDistance(t, TILESIZE);
					if (endZones.isEmpty()){
			        	LOGGER.error("endZone != 1.");
			       		throw new RuntimeException("endZone != 1");
			        }
					int destID = ((MasonGeometry) endZones.get(0)).getIntegerAttribute("ID1");
					if (dest == destID){
						return t;
					}
				}
			}
			return (Tile) tmpDests.get(random.nextInt(tmpDests.size()));			
		}
		
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
					boolean empty = (destinations.getObjectsWithinDistance(t.getGeometry().getCentroid(), TILESIZE)).isEmpty();
					if (!empty){
						if (!allTilesOfDestinations.contains(t)){
							allTilesOfDestinations.add(t);
						}
					}
					empty = (starts.getObjectsWithinDistance(t.getGeometry().getCentroid(), TILESIZE)).isEmpty();
					if (!empty){
						if (!allTilesOfStarts.contains(t)){
							allTilesOfStarts.add(t);
						}
					}
				}
			}
		}
	    private void getAllTilesOfDisplays() {
			int width = getWidthInTiles();
			int height = getHeightInTiles();
			for (int tx=0;tx< width; tx++){
				for (int ty=0;ty<height; ty++){
					Tile t = map.getTile(tx, ty);
					boolean empty = (displays.getObjectsWithinDistance(t.getGeometry().getCentroid(), TILESIZE)).isEmpty();
					if (!empty){
						if (!allTilesOfDisplays.contains(t)){
							allTilesOfDisplays.add(t);
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
		

		public int getStandardCostsForTargetTile(Tile actualPosition, Tile targetTile, int costs) {
			int x = targetTile.getX() - actualPosition.getX();
			int y = targetTile.getY() - actualPosition.getY();
			if (Math.abs(x) > maxMoveRate || Math.abs(y) > maxMoveRate){
				costs = costs + 0;
				return costs;
			}
			int i = (2*maxMoveRate+1) * (x+ maxMoveRate) + (y+ maxMoveRate);
			CostTile costTile = (CostTile) standardCosts.get(i);
			//Sicherheitsüberprüfung
			if (costTile.getX()!=x || costTile.getY()!=y){
				LOGGER.error("Fehler Kostenberechnung: actTile:({},{}),targetTile:({},{}) - i:{}",
						actualPosition.getX(),
						actualPosition.getY(),
						targetTile.getX(),
						targetTile.getY(),
						i);
			}
			costs = costs + costTile.getCosts();
			return costs;
		}
		
	    /**
	     * @param actualPosition
	     * @param owncost
	     * @return {@link ArrayList} CostTile - Elemente die außerhalb der tile-map liegen kommen nicht vor
	     */
	    private Bag calcCostsWithoutInfluences(Tile actualPosition, int owncost){
	        Bag bag = new Bag();
	        int ax = 0;
	        int ay = 0;
	        for (int x=-maxMoveRate; x < maxMoveRate+1; x++){
	        	for (int y=-maxMoveRate; y < maxMoveRate+1; y++){
	        		if(x==0 && y==0){
	        			CostTile ct = new CostTile(ax, ay, owncost);
	        			bag.add(ct);
	        			continue;
	        		} 
	        		Tile t = getTile(actualPosition.getX()+x, actualPosition.getY()+y);
	        		//Berechnung von w nach gibbs-marskjös
	        		Geometry actTilePoly = actualPosition.getGeometry();
	        		Geometry targetTilePoly = t.getGeometry();
	        		double distance = actTilePoly.getCentroid().distance(targetTilePoly.getCentroid());
	        		BigDecimal dist = BigDecimal.valueOf(distance);
	        		dist = dist.subtract(new BigDecimal("0.4"));
	        		dist = dist.pow(2);
	        		BigDecimal divisor = new BigDecimal("0.015");
	        		divisor = divisor.add(dist);
	        		BigDecimal w = new BigDecimal("1.0");
	        		w = w.divide(divisor, RoundingMode.HALF_UP);
	        		int costs = w.setScale(0, RoundingMode.HALF_UP).intValue();
	        		CostTile costt = new CostTile(ax+x, ay+y, costs);
	        		bag.add(costt);
	        	}
	        }
	        return bag;
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
	    	return numAgents; 
	    }
	    
	    public void setNumAgents(int a){ 
	    	if (a > 0) numAgents = a; 
	    }
		@Override
	    public void start(){
	        super.start();
	        results = new Results(numAgents, stadium);
	        Stoppable stoppable = schedule.scheduleRepeating(results, 1);
	        results.setStoppMe(stoppable);
	        ArrayList<Display> dl = addDisplays();
	        results.setDisplayList(dl);
	        //entferne eventuell noch vorhandene Agenten
	        agents.clear(); 
	        //füge neue Agenten hinzu
	        LOGGER.info("Instance:{}, Füge Agenten hinzu...",Long.toString(this.seed()));
	        addAgents();
	        //setze den minimum bounding rectangle anhand des Bewegungsraums
	        agents.setMBR(movingSpace.getMBR());
	    }    
	    

	    private ArrayList<Display> addDisplays() {
			Bag DisplayBag = new Bag();
			DisplayBag.addAll(allTilesOfDisplays);
			ArrayList<Display> displList = new ArrayList<Display>();
			HashMap<Tile, Path> destList = new  HashMap<Tile, Path>();
			//für alle displaypolygone
			for (Object object : displays.getGeometries()){
				Geometry g  = ((MasonGeometry) object).getGeometry();
				Tile centerTile = getTileByCoord(g.getCentroid().getX(), g.getCentroid().getY());
				Bag tilesOfMg = new Bag();
				for (Object o : DisplayBag){
		    		Tile t = (Tile) o;
		    		boolean empty = g.isWithinDistance(t.getGeometry().getCentroid(), TILESIZE);
		    		if (!empty){
		    			tilesOfMg.add(t);
		    			DisplayBag.remove(t);
		    		}
		    	}
				// ermittle alle wege zu den zielen
				HashMap<Tile, Integer> dests = centerTile.getDestinations();
				RoomAgent a = new RoomAgent(fakeAgentID, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, new Tile(0, 0), new Results(numAgents, stadium)); //fakeAgent
				for (Map.Entry<Tile, Integer> entry : dests.entrySet()){
					Tile key = entry.getKey();
					Path p = calcNewPath(a, centerTile, key);
					if (p!=null){
						destList.put(key, p);
					}
				}
				Display d = new Display(tilesOfMg, dynamic, destList);
	    		displList.add(d);
	    		d.geometry = g;
	    		Stoppable stoppable = schedule.scheduleRepeating(d);
                d.setStoppMe(stoppable);
			}
		    	
	    	for (Display d : displList){
	    		d.setDisplayList(displList);
	    	}
	    	return displList;
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
