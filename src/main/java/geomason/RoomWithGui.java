package geomason;

import java.awt.Color;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.simple.OvalPortrayal2D;

public class RoomWithGui extends GUIState{

    Display2D display;
    JFrame displayFrame;
    GeomVectorFieldPortrayal movingSpacePortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal obstaclePortrayal = new GeomVectorFieldPortrayal();
    
    public static final Logger LOGGER = LoggerFactory.getLogger(RoomWithGui.class);

    public RoomWithGui(SimState state) {
		super(state);
	}

	public RoomWithGui() {
		super(new Room(System.currentTimeMillis()));
	}
	
    @Override
    public void init(Controller controller){
        super.init(controller);
        display = new Display2D(Room.WIDTH, Room.HEIGHT, this);
        display.attach(movingSpacePortrayal, "Bewegungsraum");
        display.attach(agentPortrayal, "Agenten");
        display.attach(obstaclePortrayal, "Hindernisse");
        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    private void setupPortrayals(){
        Room roomState = (Room) state;
        agentPortrayal.setField(Room.agents);
        LOGGER.debug("anzahl agenten: {}",Room.agents.getGeometries().numObjs );
        agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.RED, 4.0)); 
        movingSpacePortrayal.setField(roomState.movingSpace);
        movingSpacePortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY,true));
        obstaclePortrayal.setField(roomState.obstacles);
        obstaclePortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK,true));
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
        RoomWithGui roomGUI = new RoomWithGui();
        Console console = new Console(roomGUI);
        console.setVisible(true);
    }


    public static String getName(){ 
    	return "Testraum"; 
    }
    @Override
    public Object getSimulationInspectedObject(){ 
    	return state; 
    }

}
