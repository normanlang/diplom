package geomason;

import static geomason.RoomAgent.fakeAgentID;
import geomason.RoomAgent.Stadium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.TileBasedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.util.Bag;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Norman Langner
 * This class constructs the static floor field in form of tiles and saves it into a
 * file to prevent recalculation for every new simulation start. 
 *
 */
public class RoomMap implements TileBasedMap{

	public static final String STATIC_MAP_TILES_CSV = "static-map_tiles.csv";

	public static final Logger LOGGER = LoggerFactory.getLogger(RoomMap.class);
	
	private int width = 0, height = 0;
	private double minX, minY;
	private Tile[][] map;
	private boolean[][] visited; 
	private Room room;
	private String tokenSep = ",";
	private String blockSep = ";";

	private boolean append = true;

	/**
	 * The constructor builds all the tiles of the {@link RoomMap}  
	 * @param state
	 */
	public RoomMap(Room state){
		room = state;
		width = room.getWidthInTiles();
		height = room.getHeightInTiles();
		room.allTilesOfMap.setFieldHeight(height);
		room.allTilesOfMap.setFieldHeight(width);
		//hole minX u. minY zur Berechnung des "Mapursprungs"
		minX = (int) Math.floor(room.movingSpace.getMBR().getMinX()); 
		minY = (int) Math.floor(room.movingSpace.getMBR().getMinY());
		map = new Tile[width][height];
		visited = new boolean[width][height];
		LOGGER.info("Starte Aufbau des Gitters");
		buildRoomMap();
		LOGGER.info("Gitter erzeugt");
	}

	/**
	 * build the room map in tiles 
	 */
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
				Tile tile = new Tile(i,j, 0);
				tile.setPolygon(poly);
				Bag obstacles = room.obstacles.getObjectsWithinDistance(poly.getCentroid(), Room.TILESIZE);
				Bag inMovingSpace = room.movingSpace.getObjectsWithinDistance(poly.getCentroid(), Room.TILESIZE);
				if (!obstacles.isEmpty() || inMovingSpace.isEmpty()){
					tile.setUsable(false);
				} else if (!inMovingSpace.isEmpty()){
					tile.setUsable(true);
				}
				map[i][j] = tile;
				room.allTilesOfMap.addGeometry(tile);
				//ändere die Höhe für das nächste Tile
				yTile = ynew;
			}
			yTile = Math.floor(minY);
			xTile = xTile + Room.TILESIZE;
		}
		
	}
	
	/**
	 * creates the static floor field for this map
	 * @param allDestinationCenterAsTiles the evacuation points as tiles
	 * @param stadium the {@link Stadium}
	 */
	public void createStaticFloorField(Bag allDestinationCenterAsTiles, RoomAgent.Stadium stadium){
		int max = width*height;
		LOGGER.trace("Start processing tiles. Width: {}, Height: {}, Tiles Gesamt: {}",
				width,
				height,
				max);
		int x = 0;
		//erzeuge fake agent fuer die Routenberechnung
		RoomAgent a = new RoomAgent(fakeAgentID, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, new Tile(0, 0, 0), new Results(room.numAgents, stadium)); //fakeAgent
		//gehe alle tiles der map durch
		for (int tx=0;tx< width; tx++){
			for (int ty=0;ty<height; ty++){
				Bag dests  = new Bag();
				dests.addAll(allDestinationCenterAsTiles);
				//map die alle destination-tiles mit der distanz (in tiles) von der aktuellen Zelle
				//zur destination-tile verknuepft
				HashMap<Tile, Integer> destinationsWithLength = new HashMap<Tile, Integer>();
				//nehme das aktuelle tile
				Tile currentTile = getTile(tx, ty);
				//wenn es benutzbar ist, berechne fuer alle destination tiles den kosten-
				//guenstigsten weg (A*)
				if (currentTile.isUsable()){
					while (!dests.isEmpty()){
						Tile destTile = (Tile) dests.pop();
						Path p = room.calcNewPath(a, currentTile, destTile);
						//p ist null, wenn kein weg berechnet werden kann
						if (p == null){
							//wenn das aktuelle tile = eines der destTiles ist, dann ist der weg 0
							if (currentTile.equals(destTile)){
								destinationsWithLength.put(destTile, 0);
							} else {
								//sonst ist der Weg der max-Integer-Wert
								destinationsWithLength.put(destTile, Integer.MAX_VALUE);
							}
							//wenn ein weg gefunden wurde, dann fuege die Distanz hinzu 
						} else{
							destinationsWithLength.put(destTile, p.getLength());
						}
					}
					currentTile.setDestinations(destinationsWithLength);
					//speichere das ergebnis zur optimierung in eine datei
					writeTileInformationToFile(stadium,currentTile);
				}
				x++;
				if (x%500 == 0){
					LOGGER.trace("Processed {} of {} tiles...",x, max);
				}
			}
		}
		LOGGER.trace("Finished processing tiles.");
	}
	
	private void writeTileInformationToFile(Stadium stadium, Tile currentTile) {
		try {
			FileWriter fw = new FileWriter(stadium.name() + "-" + STATIC_MAP_TILES_CSV,append);
			fw.write(createLine(currentTile));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("I/O-Fehler: {}", e.getMessage());
		}
	}


	private String createLine(Tile currentTile) {
		StringBuilder builder = new StringBuilder();
		builder.append(currentTile.getX());
		builder.append(tokenSep);
		builder.append(currentTile.getY());
		builder.append(blockSep);
		for (Tile destination : currentTile.getDestinations().keySet()) {
			builder.append(destination.getX());
			builder.append(tokenSep);
			builder.append(destination.getY());
			builder.append(tokenSep);
			builder.append(currentTile.getDestinations().get(destination));
			builder.append(blockSep);
		}
		builder.append("\n");
		return builder.toString();
	}


	public int getWidthInTiles() {
		return width;
	}
	public int getHeightInTiles() {
		return height;
	}

	public void pathFinderVisited(int x, int y) {
		visited[x][y] = true;
		
	}
	
	public Tile getTile(int x, int y){
		if (x > -1 && x <= width && y > -1 && y <= height){
			return map[x][y];
		} else{
			LOGGER.error("getTile(Map) is out of bounds for "+x+","+y);
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

	public boolean isBlocked(Mover mover, int x, int y) {
		Tile t = map[x][y];
		Coordinate coord = room.getCoordForTile(t);
		if (room.getAllDestinationCenterTiles().contains(t) && t.isUsable()){
			return false;
		}
		if (room.movingSpace.isCovered(coord) && t.getPotentialAgentsList().isEmpty() && t.isUsable() && t.getAddCosts() == 0){
			return false;
		}
		return true;
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		return 1;
	}




	public void readStaticFloorField(Stadium stadium) {
		BufferedReader br = null;
		try {
			FileReader fr = new FileReader(new File(stadium.name() + "-" + STATIC_MAP_TILES_CSV));
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer toki = new StringTokenizer(line, blockSep);
				String tileCoords = toki.nextToken();
				String[] coords = tileCoords.split(tokenSep);
				int x = Integer.parseInt(coords[0]),
						y = Integer.parseInt(coords[1]);
				Tile t = map[x][y];
				while (toki.hasMoreTokens()) {
					processDestinationBlock(t,toki.nextToken());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOGGER.error("Datei nicht gefunden; {}", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("I/O-Fehler: {}", e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.error("I/O-Fehler: {}", e.getMessage());
				}
			}
		}
	}


	private void processDestinationBlock(Tile t, String nextToken) {
		String[] tokens = nextToken.split(tokenSep);
		int x = Integer.parseInt(tokens[0]),
				y = Integer.parseInt(tokens[1]),
				length = Integer.parseInt(tokens[2]);
		t.addDestination(map[x][y],length);
	}
}
