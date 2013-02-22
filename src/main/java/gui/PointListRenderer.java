package gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import stadium.environment.Point;

import interfaces.IRenderer;

public class PointListRenderer implements IRenderer{
	
	private List<Point> pl;
	
	public void draw(Graphics g) {
		//Graphics2D g2d = (Graphics2D) g;
		for (Point p : pl){
			g.drawOval((int)p.getX(), (int)p.getY(), 2, 2);
			g.fillOval((int)p.getX(), (int)p.getY(), 2, 2);
		}
		
	}

	public synchronized List<Point> getPl() {
		return pl;
	}

	public synchronized void setPl(List<Point> pl) {
		this.pl = pl;
	}

}
