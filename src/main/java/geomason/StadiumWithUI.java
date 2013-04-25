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
import sim.util.gui.SimpleColorMap;

public class StadiumWithUI extends GUIState{
    Display2D display;
    JFrame displayFrame;
    GeomVectorFieldPortrayal movingSpacePortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();

    @Override
    public void init(Controller controller){
        super.init(controller);
        display = new Display2D(Stadium.WIDTH, Stadium.HEIGHT, this);
        display.attach(movingSpacePortrayal, "Bewegungsraum");
        display.attach(agentPortrayal, "Agenten");
        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    private void setupPortrayals(){
        Stadium stadiumState = (Stadium) state;
        agentPortrayal.setField(Stadium.agents);
        agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.RED, 4.0)); 
        movingSpacePortrayal.setField(stadiumState.movingSpace);
        movingSpacePortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY,true));

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
        StadiumWithUI worldGUI = new StadiumWithUI();
        Console console = new Console(worldGUI);
        console.setVisible(true);
    }
    public StadiumWithUI(SimState state){
        super(state);
    }

    public StadiumWithUI(){
        super(new Stadium(System.currentTimeMillis()));
    }

    public static String getName(){ 
    	return "Preussenstadion"; 
    }
    @Override
    public Object getSimulationInspectedObject(){ 
    	return state; 
    }

}
