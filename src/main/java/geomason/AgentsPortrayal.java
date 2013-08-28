package geomason;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.gui.SimpleColorMap;



/**
 * Portrayal fuer die agenten. faerbt diese ein, wenn sie ihr ziel durch ein display geaendert haben
 */
class AgentsPortrayal extends GeomPortrayal{
    private static final long serialVersionUID = 1L;
	private boolean dynamic;
	private Color blue, gold;
    
	public AgentsPortrayal(boolean b) {
		super();
		dynamic = b;
		blue = new Color(0,191,255);
		gold = new Color(255,215,0);
	}
    
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info){ 
        RoomAgent a = (RoomAgent) object;
        scale = 0.17;        
        if (dynamic){
        	if (a.isDisplayRecognized() == false){
                paint = Color.RED;
            } 
            if (a.isDisplayRecognized()){
                paint = blue;
            }	
        } else{
        	SimpleColorMap colorMap = new SimpleColorMap(0, 15, gold, Color.red);
            paint = colorMap.getColor(a.getPatienceCounter());
        }
        super.draw(object, graphics, info);
    }

}
