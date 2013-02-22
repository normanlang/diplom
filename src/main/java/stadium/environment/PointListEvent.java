package stadium.environment;

import java.util.EventObject;
import java.util.List;

/**
 * @author Norman Langner
 *
 */
public class PointListEvent extends EventObject{

	private static final long serialVersionUID = 1L;
	
	private List<Point> pointlist = null;
	
	public PointListEvent(Object source, List<Point> pl) {
		super(source);
		this.pointlist = pl;
	}
	
	
	/**
	 * @return point oder null
	 */
	public List<Point> getPointList(){
		return pointlist;
	}
}
