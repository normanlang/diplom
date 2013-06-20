/**
 ** RoomWithObstacle.java
 **
 ** Copyright 2011 by Sarah Wise, Mark Coletti, Andrew Crooks, and
 ** George Mason University.
 **
 ** Licensed under the Academic Free License version 3.0
 **
 ** See the file "LICENSE" for more information
 **
 * $Id: RoomWithObstacle.java 849 2013-01-08 22:56:52Z mcoletti $
 * 
 **/
package test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import geomason.MasonGeometryBlock;
import geomason.TestRoomWithObstacle;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;



/**
 * The  simulation core.
 * 
 * The simulation can require a LOT of memory, so make sure the virtual machine has enough.
 * Do this by adding the following to the command line, or by setting up your run 
 * configuration in Eclipse to include the VM argument:
 * 
 * 		-Xmx2048M
 * 
 * With smaller simulations this chunk of memory is obviously not necessary. You can 
 * take it down to -Xmx800M or some such. If you get an OutOfMemory error, push it up.
 */
public class RoomWithObstacle extends SimState
{
    private static final long serialVersionUID = 1L;

    public GeomVectorField raster = new GeomVectorField();
    public GeomVectorField movingSpace = new GeomVectorField();

    // traversable rasterGraph
    public GeomPlanarGraph rasterGraph = new GeomPlanarGraph();

    public GeomVectorField junctions = new GeomVectorField();

    // mapping between unique edge IDs and edge structures themselves
    HashMap<Integer, GeomPlanarGraphEdge> coordsToEdges =
        new HashMap<Integer, GeomPlanarGraphEdge>();

    HashMap<GeomPlanarGraphEdge, ArrayList<Agent>> edgeTraffic =
        new HashMap<GeomPlanarGraphEdge, ArrayList<Agent>>();

    public GeomVectorField agents = new GeomVectorField();

    ArrayList<Agent> agentList = new ArrayList<Agent>();
    
    // system parameter: can force agents to go to or from work at any time
    boolean goToStart = true;
    ArrayList<Integer> dest = new ArrayList<Integer>();
    ArrayList<Integer> start = new ArrayList<Integer>();


    public boolean getGoToWork()
    {
        return goToStart;
    }



    public void setGoToWork(boolean val)
    {
        goToStart = val;
    }

    // cheap, hacky, hard-coded way to identify which edges are associated with
    // goal Nodes. Done because we cannot seem to read in .shp file for goal nodes because
    // of an NegativeArraySize error? Any suggestions very welcome!
    



    /** Constructor */
    public RoomWithObstacle(long seed)
    {
        super(seed);
    }



    /** Initialization */
    @Override
    public void start()
    {
        super.start();

        // read in data
        try
        {

	        System.out.println("lese das Raster ein...");
            URL rasterFile = TestRoomWithObstacle.class.getResource("data/raster10-schnitt.shp");
            Bag rasterAttributes = new Bag();
            rasterAttributes.add("ID");
            rasterAttributes.add("COORD");            
            rasterAttributes.add("Art");
	        ShapeFileImporter.read(rasterFile, raster, rasterAttributes);
            Envelope MBR = raster.getMBR();
            // read in the tracts to create the background
	        System.out.println("lese die Vektordaten ein...");
            URL roomBoundaries = TestRoomWithObstacle.class.getResource("data/movingSpace-testroom.shp");
            Bag movingSpaceAttributes = new Bag();
            movingSpaceAttributes.add("Art");
            ShapeFileImporter.read(roomBoundaries, movingSpace, movingSpaceAttributes, MasonGeometryBlock.class);

            MBR.expandToInclude(movingSpace.getMBR());

            createNetwork();

            // update so that everyone knows what the standard MBR is
            raster.setMBR(MBR);
            movingSpace.setMBR(MBR);
            getStartAndDestPoints();
            // initialize agents
            populate();
            agents.setMBR(MBR);

            // Ensure that the spatial index is updated after all the agents
            // move
            schedule.scheduleRepeating( agents.scheduleSpatialIndexUpdater(), Integer.MAX_VALUE, 1.0);

            /** Steppable that flips Agent paths once everyone reaches their destinations*/
            Steppable flipper = new Steppable()
            {

                public void step(SimState state)
                {

                    RoomWithObstacle gstate = (RoomWithObstacle) state;

                    // pass to check if anyone has not yet reached work
                    for (Agent a : gstate.agentList)
                    {
                        if (!a.reachedDestination)
                        {
                            return; // someone is still moving: let him do so
                        }
                    }
                    // send everyone back in the opposite direction now
                    boolean toWork = gstate.goToStart;
                    gstate.goToStart = !toWork;

                    // otherwise everyone has reached their latest destination:
                    // turn them back
                    for (Agent a : gstate.agentList)
                    {
                        a.flipPath();
                    }
                }
            };
            schedule.scheduleRepeating(flipper, 10);

        } catch (FileNotFoundException e)
        {
            System.out.println("Error: missing required data file");
        }
    }



    private void getStartAndDestPoints() {
    	Bag rasterGeometries = raster.getGeometries();
    	for (int i = 0; i < rasterGeometries.size(); i++){
    		MasonGeometry mg = (MasonGeometry) rasterGeometries.get(i);
    		if (mg.getStringAttribute("Art").equalsIgnoreCase("evakuierung")){
    			dest.add(mg.getDoubleAttribute("COORD").intValue());
    		}
    		if (mg.getStringAttribute("Art").equalsIgnoreCase("Block O")){
    			start.add(mg.getDoubleAttribute("COORD").intValue());
    		}
    	}
    	
		
	}



	/** Create the road rasterGraph the agents will traverse
     *
     */
    private void createNetwork()
    {
        System.out.println("creating rasterGraph...");

        rasterGraph.createFromGeomField(raster);
        
        for (Object o : rasterGraph.getEdges())
        {
            GeomPlanarGraphEdge e = (GeomPlanarGraphEdge) o;

            coordsToEdges.put(e.getDoubleAttribute("COORD").intValue(), e);

            e.setData(new ArrayList<Agent>());
        }

        addIntersectionNodes(rasterGraph.nodeIterator(), junctions);
    }



    /**
     * Read in the population file and create an appropriate pop
     * @param filename
     */
    public void populate(){

        try{
                for (int i = 0; i < 1; i++) {
                	GeomPlanarGraphEdge startingEdge = coordsToEdges.get(start.get(random.nextInt(start.size())));
                    GeomPlanarGraphEdge goalEdge = coordsToEdges.get(dest.get(random.nextInt(dest.size())));
                    Agent a = new Agent(this, startingEdge, goalEdge);
                    boolean successfulStart = a.start(this);

                    if (!successfulStart){
                        continue; // DON'T ADD IT if it's bad
                    }
                    MasonGeometry newGeometry = a.getGeometry();
                    newGeometry.isMovable = true;
                    agents.addGeometry(newGeometry);
                    agentList.add(a);
                    schedule.scheduleRepeating(a);
                }
        } catch (Exception e){
            System.out.println("ERROR: issue with population file: " + e);
        }

    }



    /** adds nodes corresponding to road intersections to GeomVectorField
     *
     * @param nodeIterator Points to first node
     * @param intersections GeomVectorField containing intersection geometry
     *
     * Nodes will belong to a planar graph populated from LineString rasterGraph.
     */
    private void addIntersectionNodes(Iterator<?> nodeIterator,
                                      GeomVectorField intersections)
    {
        GeometryFactory fact = new GeometryFactory();
        Coordinate coord = null;
        Point point = null;
        int counter = 0;

        while (nodeIterator.hasNext())
        {
            Node node = (Node) nodeIterator.next();
            coord = node.getCoordinate();
            point = fact.createPoint(coord);

            junctions.addGeometry(new MasonGeometry(point));
            counter++;
        }
    }



    /** Main function allows simulation to be run in stand-alone, non-GUI mode */
    public static void main(String[] args)
    {
        doLoop(RoomWithObstacle.class, args);
        System.exit(0);
    }

}