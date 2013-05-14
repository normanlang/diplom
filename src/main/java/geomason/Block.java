package geomason;

final public class Block {

	final private int capacity;
	final private String blockName;
	final private boolean homeFans;
	
	public Block (String name, int cap, boolean homeFan){
		blockName = name;
		capacity = cap;
		homeFans = homeFan;
	}
	
	/**
	 * @return the homeFans
	 */
	public boolean isHomeFans() {
		return homeFans;
	}

	/**
	 * @return the blockName
	 */
	public String getBlockName() {
		return blockName;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}
}
