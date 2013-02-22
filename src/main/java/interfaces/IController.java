package interfaces;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * @author Norman Langner
 * This interface describes a controller for the gui. it needs also informations from the environment, 
 * therefor it extends a Listener interface.  
 */
public interface IController extends IListener{
	
	/**this method is called, when the menuebar-entry "open File" was selected
	 * @param e is the ActionEvent from the source which wants to open the file
	 */
	public void openActionPerformed(ActionEvent e);
	
	/** 
	 * if needed, this method can handle the exceptions
	 * @param t the thrown exception
	 */
	public void handleException(Throwable t);
	
	public void getItemInfoMouseClickPerformed(MouseEvent e);
}
