
package geomason;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class Agent implements Steppable {

    private static final long serialVersionUID = -5318720825474063385L;

    final int N  = 0; 
    final int NW = 1; 
    final int W  = 2;
    final int SW = 3;
    final int S  = 4;
    final int SE = 5; 
    final int E  = 6; 
    final int NE = 7;


    public int direction;
    public Point location = null;
    public Point destination = null;
    public double moveRate = 0.2;
    public int weight;
    public boolean homeFan;
    private MersenneTwisterFast random = new MersenneTwisterFast();;
    private int steps = 0;
    
    
    public Agent(Point dest, int d){
        direction = d;
    	destination = dest;
       	weight = calcWeight();
       	homeFan = randomFan();
    }
    
    

	public Agent(int d){
        direction = d;
       	weight = calcWeight();
       	homeFan = randomFan();
    }
    public void setLocation(Point p){ 
    	location = p; 
    }

    public Geometry getGeometry(){ 
    	return location;
    }
    
    public void step(SimState state){
    	steps++;
    	PreussenStadiumModel preussenStadiumModelState = (PreussenStadiumModel)state; 
        GeomVectorField accessableArea = preussenStadiumModelState.movingSpace;
        Coordinate coord = (Coordinate) location.getCoordinate().clone();
        AffineTransformation translate = null;
        Point tempPoint = (Point) location.clone();
        switch (direction){
            case N : 
            	translate = AffineTransformation.translationInstance(0.0, moveRate);
                coord.y += moveRate;
                tempPoint.apply(translate);
                break;
            case S : 
                translate = AffineTransformation.translationInstance(0.0, -moveRate);
                coord.y -= moveRate;
                tempPoint.apply(translate);
                break;
            case E : 
                translate = AffineTransformation.translationInstance(moveRate, 0.0);
                coord.x += moveRate;
                tempPoint.apply(translate);
                break;
            case W :
                translate = AffineTransformation.translationInstance(-moveRate, 0.0);
                coord.x -= moveRate;
                tempPoint.apply(translate);
                break;
            case NW : 
                translate = AffineTransformation.translationInstance(-moveRate,moveRate);
                coord.x -= moveRate;
                tempPoint.apply(translate);
                coord.y += moveRate; 
                break;
            case NE : 
                translate = AffineTransformation.translationInstance( moveRate, moveRate );
                coord.x += moveRate;
                tempPoint.apply(translate);
                coord.y += moveRate;
                break;
            case SW : 
                translate = AffineTransformation.translationInstance(-moveRate, -moveRate);
                coord.x -= moveRate;
                coord.y -= moveRate;
                tempPoint.apply(translate);
                break;
            case SE : 
                translate = AffineTransformation.translationInstance( moveRate, -moveRate);
                coord.x += moveRate;
                coord.y -= moveRate;
                tempPoint.apply(translate);
                break;
            }
        if (steps > 5){
        	if (accessableArea.isCovered(coord) && isNotOccupied(tempPoint)){	
            	location.apply(translate);
            } else direction = state.random.nextInt(8);
        } else location.apply(translate);
    }
    
    private boolean isNotOccupied(Point p) {
    	Bag nearbyObjects = PreussenStadiumModel.agents.getObjectsWithinDistance(location, moveRate);
    	for (int i=0; i<nearbyObjects.numObjs; i++){
    		MasonGeometry actualAgent = (MasonGeometry) nearbyObjects.get(i);
    		if (actualAgent.getGeometry().intersects(p) || actualAgent.getGeometry().touches(p)){
    			return false;
    		}
    	} 
    	return true;
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
}
