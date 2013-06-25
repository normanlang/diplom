
package geomason;

import java.util.ArrayList;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;
import org.newdawn.slick.util.pathfinding.Path.Step;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.planargraph.Node;

import ec.util.MersenneTwisterFast;
import sim.app.geo.gridlock.AStar;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;

public class Agent implements Steppable, Mover {

    private static final long serialVersionUID = -5318720825474063385L;

    public Point location = null;
    private Tile destination = null; 
    public double moveRate = 0.5;
    public int weight;
    public boolean homeFan;
    private double distance;
    private MersenneTwisterFast random = new MersenneTwisterFast();
    public static enum Stadium {PREUSSEN,TEST,ESPRIT};
    private Stadium stadium;
    private Path path;
    private int step = 0;
    private TestRoomWithObstacle testRoomModelState = null;
    private ArrayList<Tile>  pathAsTileList = new ArrayList<Tile>();
    private ArrayList<Coordinate> pathAsCoordList = new ArrayList<Coordinate>();
    GeomVectorField accessableArea = null;
    private PointMoveTo pointMoveTo = new PointMoveTo();
    
    public Agent(Stadium stadium){
       	weight = calcWeight();
       	homeFan = randomFan();
       	this.stadium = stadium;
    }
		

    
    public void step(SimState state){
        //weiter gehts
    	setStateDependingOnStadium(state);
    	//testRoomModelState.calculateLineOfSight(this);
    	moveAgent();
    	//moveAgentTest();
  
    }
    



	private int calcWeight(){
    	int[] weightArray = new int[60];
    	for (int i=0;i<weightArray.length;i++){
    		weightArray[i] = 60+i;
    	}
    	return weightArray[random.nextInt(60)];
    }
    private boolean randomFan() {
		return random.nextBoolean();
	}
    
    public void calculateNextPosition(SimState state){
    	setStateDependingOnStadium(state);
    	AffineTransformation translate = null;
    	Step nextStep = path.getStep(step+1);
    	Tile nextTile = testRoomModelState.getTileByCoord(nextStep.getX(), nextStep.getY());
    	if (nextTile.getAgentList().isEmpty()){
    		double difx = nextTile.getGeometry().getCentroid().getX() - this.getGeometry().getCoordinate().x;
    		double dify = nextTile.getGeometry().getCentroid().getY() - this.getGeometry().getCoordinate().y;
    		translate = AffineTransformation.translationInstance(difx, dify);
    		location.apply(translate);
    	} else {
    		int destX = destination.getGeometry().getCentroid().getCoordinate().X;
    		int destY = destination.getGeometry().getCentroid().getCoordinate().Y;
    		Path p = testRoomModelState.calcNewPath(this, location.getCoordinate().X, location.getCoordinate().Y, destX, destY);
    		if (p.getLength() > 0) path = p;
    	}
    		
    		/*for (int ix = -1; ix<2; ix++){
    			for (int iy = -1; iy<2; iy++){
    				if (ix==0 && iy==0) continue; //dann ist es das nextTile
    				int posx = nextStep.getX() + ix;
    				int posy = nextStep.getY() + iy;
    				Tile t = testRoomModelState.getTile(posx, posy);
    				if (t.getAgentList().contains(this)) continue; //dann ist es das aktuelle Tile
    				if (path.contains(posx, posy)){
    					double difx = t.getGeometry().getCentroid().getX() - this.getGeometry().getCoordinate().x;
    		    		double dify = t.getGeometry().getCentroid().getY() - this.getGeometry().getCoordinate().y;
    		    		translate = AffineTransformation.translationInstance(difx, dify);
    		    		location.apply(translate);
    		    		break;
    				}
    				
    			}
    		}*/
    }
    
    private void moveAgentTest(){
    	if (pathAsCoordList.isEmpty()){
    		return; //da die liste leer ist ist entweder das ziel erreicht oder kein ziel gegeben
    	}
    	System.out.println("Pos: "+location.getX()+", "+location.getY()+" noch "+pathAsTileList.size()+" Steps");
    	
    	Coordinate nextCoord = pathAsCoordList.get(0);
    	moveTo(nextCoord);
     	pathAsCoordList.remove(0);
     	
    }
    
    private void moveAgent(){
    	if (pathAsTileList.isEmpty()){
    		return; //da die liste leer ist ist entweder das ziel erreicht oder kein ziel gegeben
    	}
    	System.out.println("Pos: "+location.getX()+", "+location.getY()+" noch "+pathAsTileList.size()+" Steps");
    	Tile nextTile = pathAsTileList.get(0);
    	//Tile destTile = pathAsTileList.get(pathAsTileList.size());
    	Coordinate actCoord = location.getCoordinate();
    	Coordinate nextCoord = nextTile.getGeometry().getCentroid().getCoordinate();
    	Coordinate moveCoord = coordOnLineOfActAndNext(actCoord, nextCoord);
    	// berechne die Differenz der Punkte
    	/*double difx = moveCoord.x - actCoord.x;
    	double dify = moveCoord.y - actCoord.y;
    	AffineTransformation translate = new AffineTransformation();
    	translate  = AffineTransformation.translationInstance(difx, dify);
    	location.apply(translate);*/
    	moveTo(moveCoord);
    	System.out.println("neue Loc: "+location.getX()+", "+location.getY());
    	pathAsTileList.remove(0);
    	
    }

	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(SimState state, Path p) {
		setStateDependingOnStadium(state);
		if (p!=null){
			this.path = p;
			//packe alle tiles in eine arraylist zum einfache
			setPathAsTileList();
			setpathasCoordList();
		}
	}
	
	/**
	 * calculates the point on the line between actual position and next position depending on the moveRate
	 * @param actCoord the actual position
	 * @param nextCoord the next position
	 * @return the 
	 */
	private Coordinate coordOnLineOfActAndNext(Coordinate actCoord, Coordinate nextCoord){
		//berechne Richtungsvektor
		double rx = nextCoord.x - actCoord.x;
		double ry = nextCoord.y - actCoord.y;
		//berechne norm des richtungsvektors
		double norm;
		if (rx == 0 && ry == 0){
			norm = 1;
		} else norm = 1 / Math.sqrt(rx * rx + ry * ry);
		//berechne gesuchten Punkt der auf der geraden AP-EP liegt und den Abstand der moveRate vom 
		//actPoint hat
		double xm = actCoord.x + moveRate * norm *rx;
		double ym = actCoord.y + moveRate * norm *ry;
		Coordinate c = new Coordinate(xm, ym);
		return c;
	}
	
    // bewegt den Agenten zu den gegebenen Koordinaten
    public void moveTo(Coordinate c)
    {
        pointMoveTo.setCoordinate(c);
        location.apply(pointMoveTo);
    }
    
    private void setPathAsTileList(){
    	for (int i=0; i< path.getLength();i++){
			Step step = path.getStep(i);
			System.out.println(step.getX()+", "+step.getY());
			Tile t = testRoomModelState.getTile(step.getX(), step.getY());
			pathAsTileList.add(i, t);
		}
    }
    
    private void setpathasCoordList(){
    	for (int i=0; i< path.getLength();i++){
			Step step = path.getStep(i);
			System.out.println(step.getX()+", "+step.getY());
			Tile t = testRoomModelState.getTile(step.getX(), step.getY());
			Coordinate c = t.getGeometry().getCentroid().getCoordinate();
			pathAsCoordList.add(i, c);
		}
    }
    
	private void setStateDependingOnStadium(SimState state){
    	switch (stadium){
    	case PREUSSEN: //für Preussenstadion
    		PreussenStadiumModel preussenStadiumModelState = (PreussenStadiumModel)state; 
            accessableArea = preussenStadiumModelState.movingSpace;
            break;
    	case TEST:         //für Testroom
    		testRoomModelState = (TestRoomWithObstacle)state; 
            accessableArea = testRoomModelState.movingSpace;
            break;
    	case ESPRIT: 
    		System.out.println("noch nicht fertig");
    		break;
    	default: 
    		System.out.println("es wurde kein Stadium ausgewählt");
    		break;
    	}
	}
	
    public void setLocation(Point p){ 
    	location = p; 
    }

    public Geometry getGeometry(){ 
    	return location;
    }
}
