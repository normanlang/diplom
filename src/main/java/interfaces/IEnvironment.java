package interfaces;

import java.util.List;

import javax.swing.tree.TreeModel;

import stadium.environment.Point;

public interface IEnvironment {

	public void addIListener(IListener alistener);
	
	public void removeIListener(IListener alistener);
	
	public List<Point> getPointList();
	
	public void setPointList(List<Point> pl);
	
	public Point inCircle(java.awt.Point p);
	

}
