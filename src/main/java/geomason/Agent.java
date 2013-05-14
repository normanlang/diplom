
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
    private MersenneTwisterFast random;
    
    
    public Agent(Point dest){
        destination = dest;
       	weight = calcWeight();
    }
    
    public Agent(int d){
        direction = d;
       	weight = calcWeight();
    }
    public void setLocation(Point p){ 
    	location = p; 
    }

    public Geometry getGeometry(){ 
    	return location;
    }
    
    public void step(SimState state){            
    	PreussenStadiumModel preussenStadiumModelState = (PreussenStadiumModel)state; 
        GeomVectorField accessableArea = preussenStadiumModelState.movingSpace;
        Coordinate coord = (Coordinate) location.getCoordinate().clone();
        AffineTransformation translate = null;
        switch (direction){
            case N : 
            	translate = AffineTransformation.translationInstance(0.0, moveRate);
                coord.y += moveRate;
                break;
            case S : 
                translate = AffineTransformation.translationInstance(0.0, -moveRate);
                coord.y -= moveRate;
                break;
            case E : 
                translate = AffineTransformation.translationInstance(moveRate, 0.0);
                coord.x += moveRate;
                break;
            case W :
                translate = AffineTransformation.translationInstance(-moveRate, 0.0);
                coord.x -= moveRate;
                break;
            case NW : 
                translate = AffineTransformation.translationInstance(-moveRate,moveRate);
                coord.x -= moveRate;
                coord.y += moveRate; 
                break;
            case NE : 
                translate = AffineTransformation.translationInstance( moveRate, moveRate );
                coord.x += moveRate;
                coord.y += moveRate;
                break;
            case SW : 
                translate = AffineTransformation.translationInstance(-moveRate, -moveRate);
                coord.x -= moveRate;
                coord.y -= moveRate;
                break;
            case SE : 
                translate = AffineTransformation.translationInstance( moveRate, -moveRate);
                coord.x += moveRate;
                coord.y -= moveRate;
                break;
            }
        
        if (accessableArea.isCovered(coord)){
        	Bag test = PreussenStadiumModel.agents.getObjectsWithinDistance(location, moveRate);
        	location.apply(translate);
        }
        else direction = state.random.nextInt(8);
    }
    
    private int calcWeight(){
    	int[] weightArray = new int[60];
    	for (int i=0;i<weightArray.length;i++){
    		weightArray[i] = 60+i;
    	}
    	random = new MersenneTwisterFast();
    	return weightArray[random.nextInt(60)];
    }
    
    private boolean isSomeoneThere(Coordinate c){
    	
    	return false;
    }
}
