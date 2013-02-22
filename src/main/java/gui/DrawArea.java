package gui;

import interfaces.IRenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class DrawArea extends JPanel{
	
	List<IRenderer> drawables;
	public DrawArea(){
		this.setDrawArea();
	}
	
	private void setDrawArea(){
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setPreferredSize(new Dimension(700, 600));			
	}
	
	public void paintComponent (Graphics g){ //TODO: wird wahrscheinlich schon beim erzeugen mit ausgeführt..
		super.paintComponent(g);
		if (drawables != null){
			for (IRenderer drawable : drawables){
				drawable.draw(g);
			}
		}
		
	}

	public synchronized List<IRenderer> getDrawables() {
		return drawables;
	}

	public synchronized void setDrawables(List<IRenderer> drawables) {
		this.drawables = drawables;
		repaint();
	}
}
