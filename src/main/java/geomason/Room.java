package geomason;

import geomason.RoomAgent.Stadium;

import java.io.FileNotFoundException;
import java.net.URL;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;


public class Room extends SimState{

		private static final long serialVersionUID = -1430063512195387977L;
		
		public static final double TILESIZE = 0.5;
		public static final int WIDTH = 800; 
		public static final int HEIGHT = 600;
	    public static int NUM_AGENTS = 20;
	    public static GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);   
	    public GeomVectorField movingSpace = new GeomVectorField(WIDTH, HEIGHT);
	    public GeomVectorField obstacles = new GeomVectorField(WIDTH, HEIGHT);
	    private TestRoomMap map;
	    public double dmax = 15.0;
	    
		public Room(long seed) {
			super(seed);
	        loadData();
	        map = new TestRoomMap(this);
		}
		
		   private void addAgents(){
			   int e =0;
		        for (int i = 0; i < NUM_AGENTS; i++){
		        	if (movingSpace.getGeometries().isEmpty()){
		        		throw new RuntimeException("No polygons found.");
		            }
		        	//lege startkoordinaten fest
		        	int xs = 405496, ys = 5754179;
		        	//lege zielkoordinaten fest
		        	int xd = 405574, yd = 5754222;
		        	//hole die entsprechenden tiles
		        	RoomAgent a = new RoomAgent(i, Stadium.TEST);
		        	int d = i%10;
		        	if (d==0) e++; 
		        	Tile startTile = getTileByCoord(xs-e, ys+d);
		        	Tile endTile = getTileByCoord(xd, yd);
		        	Path p = calcNewPath(a, startTile, endTile);
		        	if (p!=null){
		        		a.setPath(this, p);
		        	}
		        	Point loc = new GeometryFactory().createPoint(new Coordinate(405496.82-e , 5754179.10+d));
			    	a.setLocation(loc);
			    	MasonGeometry mg = new MasonGeometry(a.getGeometry());
			    	mg.isMovable = true;
	    			agents.addGeometry(mg);
	                schedule.scheduleRepeating(a);
		        }        
		    }

		    //grund- und hilfsfunktionen
		    
			private void loadData(){
	        // url für die vektordaten der Zonen und des Bewegungsraums
			URL testRoomBoundaries = TestRoomWithObstacle.class.getResource("data/movingSpace-testroom.shp");
			URL obstacleBoundaries = TestRoomWithObstacle.class.getResource("data/hindernisse.shp");
	        Bag movingSpaceAttributes = new Bag();
	        movingSpaceAttributes.add("Art");     
	        //lese vom Vektorlayer noch Attribute aus der shp-Datei aus

			try{
		        //lese den vector-layer des Raums und der Zonen ein
		        System.out.println("lese die Vektordaten ein...");
	            ShapeFileImporter.read(testRoomBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryBlock.class);
	            ShapeFileImporter.read(obstacleBoundaries, obstacles);
	        } catch (FileNotFoundException ex){
	            System.out.println("ShapeFile import failed");
	        }
			//sicher stellen, dass beide das gleiche minimum bounding rectangle(mbr) haben
			Envelope MBR = movingSpace.getMBR();
			obstacles.setMBR(MBR);
			obstacles.computeConvexHull();
			movingSpace.computeConvexHull();
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
			if ( newPath == null){
				System.err.println("Keinen Pfad gefunden");
			}
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


}
