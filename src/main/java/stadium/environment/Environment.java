package stadium.environment;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;
import interfaces.IEnvironment;
import interfaces.IListener;

public class Environment implements IEnvironment {

	private EventListenerList listenerList = null;
	private List<Point> pointList;
	private final int radius = 5;
	
	public Environment(){
		super();
		listenerList = new EventListenerList();
	}
	public void addIListener(IListener alistener) {
		listenerList.add(IListener.class, alistener);
	}

	public void removeIListener(IListener alistener) {
		listenerList.remove(IListener.class, alistener);
	}
	
	protected void firePointListStateChange(){
	     // get registered listener
	     Object[] listeners = listenerList.getListenerList();
	     // create the necessary event
	     PointListEvent event = new PointListEvent(this, getPointList());
	     // inform all listener in reverse order
	     for (int i = listeners.length-2; i>=0; i-=2) 
	     {
	         if (listeners[i]==IListener.class) 
	             ((IListener)listeners[i+1]).itemStateChanged(event);
	     }
	}
	

	public synchronized List<Point> getPointList() {
		return pointList;
	}

	public synchronized void setPointList(List<Point> pl) {
		if (pointList == null){
			pointList = pl;
			firePointListStateChange();
		} else {
			boolean hasListChanged = !(pointList.containsAll(pl));
			if(hasListChanged){
				pointList = pl;
				firePointListStateChange();
			}
		}
	}
		
	/**
	 * tests if the Point p is in one of the circles of the points in pointList 
	 * @param p, point to test 
	 * @return the point in which circle p is, if none it returns null
	 */
	public synchronized Point inCircle(java.awt.Point p){
		if (pointList != null && p != null){
			for (Point pc : pointList){
				double abstand  = Math.sqrt((p.getX() - pc.getX()) * (p.getX() - pc.getX()) + (p.getY() - pc.getY()) * (p.getY() - pc.getY()));
				if ((int)abstand <=radius){
					return pc;
				}
			}
		}
		return null;
	}
}
