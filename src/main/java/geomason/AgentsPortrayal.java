package geomason;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.gui.SimpleColorMap;



/**
 * @author Norman Langner
 * {@link Portrayal} for the agents. Sets certain colors, if they changed their destination because they saw a display.
 */
class AgentsPortrayal extends GeomPortrayal{
    private static final long serialVersionUID = 1L;
	private boolean dynamic;
	private Color blue, gold;
    
	/**
	 * Sets the colour for the agents, depending if b is true (dynamic displays) or false. 
	 * 
	 * @param b 
	 */
	public AgentsPortrayal(boolean b) {
		super();
		dynamic = b;
		blue = new Color(0,191,255);
		gold = new Color(255,215,0);
	}
	/* (non-Javadoc)
     * @see sim.portrayal.geo.GeomPortrayal#draw(java.lang.Object, java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
     */
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
