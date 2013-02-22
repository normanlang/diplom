package stadium.environment;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Line extends Line2D{
	
	private final int p1X, p2X, p1Y, p2Y;
	
	public Line(Point p1, Point p2){
		p1X = (int)p1.getX();
		p2X = (int)p2.getX();
		p2Y = (int)p2.getY();
		p1Y = (int)p1.getY();
	}

	public Rectangle2D getBounds2D() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getX1() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getY1() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Point2D getP1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getX2() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getY2() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Point2D getP2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLine(double x1, double y1, double x2, double y2) {
		// TODO Auto-generated method stub
		
	}

}
