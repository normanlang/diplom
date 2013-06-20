
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
    int step = 0;
    private TestRoomWithObstacle testRoomModelState = null;
    
    public Agent(Stadium stadium, Tile dest){
       	weight = calcWeight();
       	homeFan = randomFan();
       	this.stadium = stadium;
       	destination = dest;
    }
		
    public void setLocation(Point p){ 
    	location = p; 
    }

    public Geometry getGeometry(){ 
    	return location;
    }
    
    public void step(SimState state){
    	GeomVectorField accessableArea = null;
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
        //weiter gehts
    	testRoomModelState.calculateLineOfSight(this);
     step++;
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
    
    public void calculateNextPosition(){
    	AffineTransformation translate = null;
    	Step nextStep = path.getStep(step+1);
    	Tile nextTile = testRoomModelState.getTile(nextStep.getX(), nextStep.getY());
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
    


	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(Path path) {
		this.path = path;
	}
}
