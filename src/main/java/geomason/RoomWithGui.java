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
import sim.util.gui.SimpleColorMap;
/**
 * @author Norman Langner
 * This class draws the simulation in 2 frames, the simulation-frame and 
 * the control-frame. See Mason-manual for more informations
 */
public class RoomWithGui extends GUIState{

    Display2D display;
    JFrame displayFrame;
    GeomVectorFieldPortrayal movingSpacePortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal obstaclePortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal displayPortrayal = new GeomVectorFieldPortrayal();
    GeomVectorFieldPortrayal tilePortrayal = new GeomVectorFieldPortrayal();
    
    /**
     * The logger for the simulation
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(RoomWithGui.class);
    /**
     * Constructor
     * @param state
     */
    public RoomWithGui(SimState state) {
		super(state);
	}
    /**
	 * Constructor, which sets up a {@link Room} as simulation
	 */
	public RoomWithGui() {
		super(new Room(System.currentTimeMillis()));
	}
	
    /* (non-Javadoc)
     * @see sim.display.GUIState#init(sim.display.Controller)
     */
    @Override
    public void init(Controller controller){
        super.init(controller);
        display = new Display2D(Room.WIDTH, Room.HEIGHT, this);
        display.attach(movingSpacePortrayal, "Bewegungsraum");
        display.attach(tilePortrayal, "Tiles");
        display.attach(agentPortrayal, "Agenten");
        display.attach(obstaclePortrayal, "Hindernisse");
        display.attach(displayPortrayal, "Displays");
        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }
    
    /**
     * Sets up the different {@link Portrayal}. Every layer has its own portrayal.
     */
    private void setupPortrayals(){
        Room roomState = (Room) state;
        agentPortrayal.setField(Room.agents);
        LOGGER.debug("anzahl agenten: {}",Room.agents.getGeometries().numObjs );
//        agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.RED, 4.0)); 
        agentPortrayal.setPortrayalForAll(new AgentsPortrayal(roomState.isDynamic()));
        movingSpacePortrayal.setField(roomState.movingSpace);
        movingSpacePortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY,true));
        obstaclePortrayal.setField(roomState.obstacles);
        obstaclePortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK,true));
        displayPortrayal.setField(roomState.displays);
        displayPortrayal.setPortrayalForAll(new GeomPortrayal(Color.GREEN,false));
        tilePortrayal.setField(roomState.allTilesOfMap);
        Color lightgrey = new Color(211,211,211);
        Color darkgreen = new Color(0, 100, 0);
        tilePortrayal.setPortrayalForAll(new TilePortrayal(new SimpleColorMap(0, 1000, lightgrey, darkgreen)));
        display.reset();
        display.setBackdrop(Color.WHITE);
        display.repaint();
    }


    /* (non-Javadoc)
     * @see sim.display.GUIState#quit()
     */
    @Override
    public void quit(){
        super.quit();
        if (displayFrame!=null){
            displayFrame.dispose();
        }
        displayFrame = null;
        display = null;
    }

    /* (non-Javadoc)
     * @see sim.display.GUIState#start()
     */
    @Override
    public void start(){
        super.start();
        setupPortrayals();
    }
    
    /**
     * Main program
     * @param args
     */
    public static void main(String[] args){
        RoomWithGui roomGUI = new RoomWithGui();
        Console console = new Console(roomGUI);
        console.setVisible(true);
    }

    /**
     * The name, which is shown at the gui-window
     * @return
     */
    public static String getName(){ 
    	return "Testraum"; 
    }
    /* (non-Javadoc)
     * @see sim.display.GUIState#getSimulationInspectedObject()
     */
    @Override
    public Object getSimulationInspectedObject(){ 
    	return state; 
    }

}
