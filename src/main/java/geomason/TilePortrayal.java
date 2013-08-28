package geomason;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.gui.SimpleColorMap;

public class TilePortrayal extends GeomPortrayal{

	private static final long serialVersionUID = 6026649920581400781L;

	SimpleColorMap colorMap = null; 
	
	public TilePortrayal(SimpleColorMap map) 
	{
		super(true); 
		colorMap = map; 
	}
	
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    	Tile t = (Tile) object;
    	if (t.isUsable()){
    		paint = colorMap.getColor(t.getAddCosts());
    	}else {
    		paint = Color.white;
    	}
        super.draw(object, graphics, info);    
    }
}
