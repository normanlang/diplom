package geomason;

import static geomason.RoomAgent.fakeAgentID;

import java.math.BigDecimal;
import java.util.HashMap;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class TestRoomMap implements TileBasedMap {

	private int width = 0, height = 0;
	private double minX, minY;
	private Tile[][] map;
	private boolean[][] visited; 
	TestRoomWithObstacle testRoom;
	Room room;
	
	public TestRoomMap(TestRoomWithObstacle state){
		testRoom = state;
		width = testRoom.getWidthinTiles();
		height = testRoom.getHeightinTiles();
		//hole minX u. minY zur Berechnung des "Mapursprungs"
		minX = (int) Math.floor(testRoom.movingSpace.getMBR().getMinX()); 
		minY = (int) Math.floor(testRoom.movingSpace.getMBR().getMinY());
		map = new Tile[width][height];
		visited = new boolean[width][height];
		buildMap();
	}
	
	public TestRoomMap(Room state){
		room = state;
		width = room.getWidthInTiles();
		height = room.getHeightInTiles();
		//hole minX u. minY zur Berechnung des "Mapursprungs"
		minX = (int) Math.floor(room.movingSpace.getMBR().getMinX()); 
		minY = (int) Math.floor(room.movingSpace.getMBR().getMinY());
		map = new Tile[width][height];
		visited = new boolean[width][height];
		buildRoomMap();
	}

	private void buildRoomMap() {
		double xTile = Math.floor(minX);
		double yTile = Math.floor(minY);
		for (int i = 0; i< width; i++){
			//um Rundungsfehler und die Ungenauigkeit von Double auszuschliessen wird mit
			//BigDecimal gearbeitet
			
			//double in BigDec umwandeln
			BigDecimal tmpx = BigDecimal.valueOf(xTile); 
			//auf 2 Kommastellen reduzieren und wenn nötig runden
			BigDecimal newx = tmpx.setScale(2, BigDecimal.ROUND_HALF_UP);
			//tilesize hinzu addieren
			newx = newx.add(BigDecimal.valueOf(Room.TILESIZE)); //tilesize hinzuaddieren
			//das ergebnis auf eine Nachkommastelle reduzieren und daraus ein double machen
			double xnew = newx.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue(); 
			for(int j=0; j< height; j++){
				BigDecimal tmpy = BigDecimal.valueOf(yTile);
				BigDecimal newy = tmpy.setScale(2, BigDecimal.ROUND_HALF_UP);
				newy = newy.add(BigDecimal.valueOf(Room.TILESIZE));
				double ynew = newy.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				//baue ein Polygon was das Tile darstellen soll als Quadrat mit einer Kantenlänge von 1m
				Coordinate p1 = new Coordinate(xTile, yTile);
				Coordinate p2 = new Coordinate(xnew, yTile);
				Coordinate p3 = new Coordinate(xnew, ynew);
				Coordinate p4 = new Coordinate(xTile, ynew);
				Coordinate[] points = {p1, p2, p3, p4, p1};
				LinearRing lr = new GeometryFactory().createLinearRing(points);
				Polygon poly = new GeometryFactory().createPolygon(lr);
				Tile tile = new Tile(i,j);
				tile.setPolygon(poly);
				Bag obstacles = room.obstacles.getObjectsWithinDistance(poly, Room.TILESIZE);
				if (!obstacles.isEmpty()){
					tile.setUsable(false);
				} else tile.setUsable(true);
				map[i][j] = tile;
				//ändere die Höhe für das nächste Tile
				yTile = ynew;
			}
			yTile = Math.floor(minY);
			xTile = xTile + Room.TILESIZE;
		}
		
	}
			
	private void buildMap() {
		int xTile = (int) Math.floor(minX);
		int yTile = (int) Math.floor(minY);
		for (int i = 0; i< width; i++){
			for(int j=0; j< height; j++){
				
				//baue ein Polygon was das Tile darstellen soll als Quadrat mit einer Kantenlänge von 1m
				Coordinate p1 = new Coordinate(xTile, yTile);
				Coordinate p2 = new Coordinate(xTile + 1, yTile);
				Coordinate p3 = new Coordinate(xTile, yTile + 1);
				Coordinate p4 = new Coordinate(xTile+ 1, yTile+ 1);
				Coordinate[] points = {p1, p2, p3, p4, p1};
				LinearRing lr = new GeometryFactory().createLinearRing(points);
				Polygon poly = new GeometryFactory().createPolygon(lr);
				Tile tile = new Tile(i,j);
				tile.setPolygon(poly);
				map[i][j] = tile;
				//ändere die Höhe für das nächste Tile
				yTile = yTile + 1;
			}
			xTile = xTile + 1;
		}
		
	}
	
	public void createStaticFloorField(Bag allDestinationCenterAsTiles, RoomAgent.Stadium stadium){
		//gehe alle tiles der map durch
		int x = 0;
		RoomAgent a = new RoomAgent(fakeAgentID, stadium, 1);
		for (int tx=0;tx< width; tx++){
			for (int ty=0;ty<height; ty++){
				Bag dests  = new Bag();
				dests.addAll(allDestinationCenterAsTiles);
				HashMap<Tile, Integer> destinationsWithLength = new HashMap<Tile, Integer>(); 
				Tile t = getTile(tx, ty);
				if (t.isUsable()){
					while (!dests.isEmpty()){
						Tile destTile = (Tile) dests.pop();
						Path p = room.calcNewPath(a, t, destTile);
						if (p == null){
							destinationsWithLength.put(destTile, Integer.MAX_VALUE);
						} else destinationsWithLength.put(destTile, p.getLength());
					}
					t.setDestinations(destinationsWithLength);
				}
				x++;
				if (x%100 == 0){
					System.out.println(x);
				}
			}
		}
		
	}
	
	public int getWidthInTiles() {
		return width;
	}
	public int getHeightInTiles() {
		// TODO Auto-generated method stub
		return height;
	}

	public void pathFinderVisited(int x, int y) {
		visited[x][y] = true;
		
	}
	
	public Tile getTile(int x, int y){
		if (x > -1 && x <= width && y > -1 && y <= height){
			return map[x][y];
		} else{
			System.out.println("getTile(Map) is out of bounds for "+x+","+y);
			return null;
		}
		
	}
	public boolean visited(int x, int y) {
		return visited[x][y];
	}

	public void clearVisited() {
		for (int x=0;x<getWidthInTiles();x++) {
			for (int y=0;y<getHeightInTiles();y++) {
				visited[x][y] = false;
			}
		}
	}

	public boolean blocked(Mover mover, int x, int y) {
		Tile t = map[x][y];
		RoomAgent a = (RoomAgent) mover;
		Coordinate coord = room.getCoordForTile(t);
		if (room.movingSpace.isCovered(coord) && t.getAgentList().isEmpty() && t.isUsable()){
			return false;
		}  
		if (a.getPath() != null && room.movingSpace.isCovered(coord) && t.isUsable()){
			Step step = a.getPath().getStep(a.getPath().getLength()-1);
			if ( x == step.getX() && y == step.getY() && !(t.getAgentList().isEmpty())){
				return false;
			}
			
		}
		return true;
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		// TODO Auto-generated method stub
		return 1;
	}
}
