package geomason;

import java.awt.Color;
import javax.swing.JFrame;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import sim.util.gui.SimpleColorMap;

public class PreussenStadiumWithUI extends GUIState{
    Display2D display;
    JFrame displayFrame;
    GeomVectorFieldPortrayal movingSpacePortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();
    private Bag agentsBag;

    @Override
    public void init(Controller controller){
        super.init(controller);
        display = new Display2D(PreussenStadiumModel.WIDTH, PreussenStadiumModel.HEIGHT, this);
        display.attach(movingSpacePortrayal, "Bewegungsraum");
        display.attach(agentPortrayal, "Agenten");
        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    private void setupPortrayals(){
        PreussenStadiumModel preussenStadiumModelState = (PreussenStadiumModel) state;

        agentPortrayal.setField(PreussenStadiumModel.agents);
        agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.RED, 4.0)); 
        agentPortrayal.setPortrayalForObject(agentPortrayal, new OvalPortrayal2D(Color.RED,4.0));
        movingSpacePortrayal.setField(preussenStadiumModelState.movingSpace);
        movingSpacePortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY,true));
        agentsBag = PreussenStadiumModel.agents.getGeometries();
        /*agentPortrayal.setField(PreussenStadiumModel.agents);
        while (!agentsBag.isEmpty()){
        	MasonGeometry mg = (MasonGeometry) agentsBag.pop();
        	double circleRadius = mg.getIntegerAttribute("weight")/32;
        	OvalPortrayal2D o = new OvalPortrayal2D(Color.RED, circleRadius);
        	agentPortrayal.setPortrayalForObject(mg, o);
        } */     
        display.reset();
        display.setBackdrop(Color.WHITE);
        display.repaint();
    }


    @Override
    public void quit(){
        super.quit();
        if (displayFrame!=null){
            displayFrame.dispose();
        }
        displayFrame = null;
        display = null;
    }

    @Override
    public void start(){
        super.start();
        setupPortrayals();
    }

    public static void main(String[] args){
        PreussenStadiumWithUI stadiumGUI = new PreussenStadiumWithUI();
        Console console = new Console(stadiumGUI);
        console.setVisible(true);
    }
    public PreussenStadiumWithUI(SimState state){
        super(state);
    }

    public PreussenStadiumWithUI(){
        super(new PreussenStadiumModel(System.currentTimeMillis()));
    }

    public static String getName(){ 
    	return "Preussenstadion"; 
    }
    @Override
    public Object getSimulationInspectedObject(){ 
    	return state; 
    }

}
