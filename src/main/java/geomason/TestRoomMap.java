package geomason;

import org.newdawn.slick.util.pathfinding.Mover;
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
	Tile[][] map;
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
			for(int j=0; j< height; j++){
				
				//baue ein Polygon was das Tile darstellen soll als Quadrat mit einer Kantenlänge von 1m
				Coordinate p1 = new Coordinate(xTile, yTile);
				Coordinate p2 = new Coordinate(xTile + Room.TILESIZE, yTile);
				Coordinate p3 = new Coordinate(xTile+ Room.TILESIZE, yTile+ Room.TILESIZE);
				Coordinate p4 = new Coordinate(xTile, yTile + Room.TILESIZE);
				Coordinate[] points = {p1, p2, p3, p4, p1};
				LinearRing lr = new GeometryFactory().createLinearRing(points);
				Polygon poly = new GeometryFactory().createPolygon(lr);
				Tile tile = new Tile(i,j);
				tile.setPolygon(poly);
				testLineString(tile, poly, lr);
				Bag obstacles = room.obstacles.getObjectsWithinDistance(poly, Room.TILESIZE);
				if (!obstacles.isEmpty()){
					tile.setUsable(false);
					System.out.println("nicht leer");
				} else tile.setUsable(true);
				map[i][j] = tile;
				//ändere die Höhe für das nächste Tile
				yTile = yTile + Room.TILESIZE;
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
		Coordinate coord = room.getCoordForTile(t);
		if (room.movingSpace.isCovered(coord) && t.getAgentList().isEmpty() && t.isUsable()){
			return false;
		} else return true;
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		// TODO Auto-generated method stub
		return 1;
	}
	
	private void testLineString(Tile tile, Polygon poly, LinearRing lr) {
		Bag obstcls = room.obstacles.getGeometries();
		MasonGeometry mg = (MasonGeometry) obstcls.get(0);
		LineString l = (LineString) mg.getGeometry();
		Point p = l.getStartPoint();
		Point p1 = l.getEndPoint();
		if (tile.getX() == 405551-(int) Math.floor(room.movingSpace.getMBR().getMinX()) &&  tile.getY() == 5754219-(int)Math.floor(room.movingSpace.getMBR().getMinY())){
			System.out.println("Poly- covers:"+mg.getGeometry().covers(poly)+" distanz: "+mg.getGeometry().distance(poly)+" contains:"
					+mg.getGeometry().contains(poly)+" intersects:"+mg.getGeometry().intersects(poly));
			System.out.println("Poly- covers:"+mg.getGeometry().covers(lr)+" distanz: "+mg.getGeometry().distance(lr)+" contains:"
								+mg.getGeometry().contains(lr)+" intersects:"+mg.getGeometry().intersects(lr));
		}
		if (tile.getX() == 405575-(int) Math.floor(room.movingSpace.getMBR().getMinX()) && tile.getY() == 5754209-(int) Math.floor(room.movingSpace.getMBR().getMinY())){
			System.out.println("Poly- covers:"+mg.getGeometry().covers(poly)+" distanz: "+mg.getGeometry().distance(poly)+" contains:"
					+mg.getGeometry().contains(poly)+" intersects:"+mg.getGeometry().intersects(poly));
			System.out.println("Poly- covers:"+mg.getGeometry().covers(lr)+" distanz: "+mg.getGeometry().distance(lr)+" contains:"
								+mg.getGeometry().contains(lr)+" intersects:"+mg.getGeometry().intersects(lr));
		}

	}
}
