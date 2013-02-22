package interfaces;
import java.util.EventListener;

import stadium.environment.PointListEvent;


/**
 * @author Norman Langner
 *
 *	This interface describes a Listener for the Environment, which wants to get updated about 
 *  changes in the environment.  
 */
public interface IListener extends EventListener{
	
	/**
	 * if an item changed its state, this method is called
	 * @param pEvent the Event which has changed from the environment
	 */
	public void itemStateChanged(PointListEvent pEvent);

}
