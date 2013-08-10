package examples;

import sim.field.geo.GeomVectorField;

public interface RoomInterface {
	
	/**
	 * @return the nUM_AGENTS
	 */
	public int getNUM_AGENTS();

	/**
	 * @return the movingSpace
	 */
	public GeomVectorField getMovingSpace();

	/**
	 * @return the obstacles
	 */
	public GeomVectorField getObstacles();

	/**
	 * @return the destinations
	 */
	public GeomVectorField getDestinations();

	/**
	 * @return the starts
	 */
	public GeomVectorField getStarts();

	/**
	 * @return the maxMoveRate
	 */
	public int getMaxMoveRateInTiles();
	
	/**
	 * @return the maxPatience
	 */
	public int getMaxPatience();
}
