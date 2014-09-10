package examples;

import sim.field.geo.GeomVectorField;

public interface RoomInterface {
	
	/**
	 * Returns the number of agents in this simulation
	 * @return the NUM_AGENTS
	 */
	public int getNUM_AGENTS();

	/**
	 * Returns the moving space of the agents
	 * 
	 * @return the movingSpace
	 */
	public GeomVectorField getMovingSpace();

	/**
	 * Returns all displays 
	 * @return the displays
	 */	
	public GeomVectorField getDisplays();

	/**
	 * Returns all obstacles 
	 * @return the obstacles
	 */
	public GeomVectorField getObstacles();

	/**
	 * Returns all destinations
	 * @return the destinations
	 */
	public GeomVectorField getDestinations();

	/**
	 * returns all starting areas
	 * @return the starts
	 */
	public GeomVectorField getStarts();

	/**
	 * returns the maximum moving rate in tiles of the agents 
	 * @return the maxMoveRate
	 */
	public int getMaxMoveRateInTiles();
	
	/**
	 * returns the maximum patience the agents have
	 * @return the maxPatience
	 */
	public int getMaxPatience();
}
