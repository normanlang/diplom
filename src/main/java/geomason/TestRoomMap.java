package geomason;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class TestRoomMap implements TileBasedMap {

	private int width = 0, height = 0;
	private double minX, minY;
	Tile[][] map;
	private boolean[][] visited; 
	TestRoomWithObstacle room;
	
	public TestRoomMap(TestRoomWithObstacle state){
		room = state;
		width = room.getWidthinTiles();
		height = room.getHeightinTiles();
		//hole minX u. minY zur Berechnung des "Mapursprungs"
		minX = room.movingSpace.getMBR().getMinX(); 
		minY = room.movingSpace.getMBR().getMinY();
		map = new Tile[width][height];
		visited = new boolean[width][height];
		buildMap();
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
				if (room.movingSpace.isCovered(tile)){
					tile.setUsable(true);
					System.out.println("i: "+i+" j: "+j);
				}
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
		if (t.getAgentList().isEmpty()){
			return false;
		} else return true;
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		// TODO Auto-generated method stub
		return 1;
	}

}