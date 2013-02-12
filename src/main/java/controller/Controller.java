package controller;

import gui.Gui;
import gui.PointListRenderer;
import interfaces.IEnvironment;
import interfaces.IController;
import interfaces.IRenderer;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import stadium.environment.Point;
import stadium.environment.PointListEvent;

public class Controller implements IController{

	private Gui gui;
	private IEnvironment env;
	private List<Point> pList;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Controller c = new Controller();
					stadium.environment.Environment e = new stadium.environment.Environment();
					//Gui g = new Gui(c, e);
					c.setEnvironment(e);
					Gui g = new Gui();
					c.setGui(g);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Controller(IEnvironment e, Gui g){
		env = e;
		gui = g;
	}
	public Controller() {
	}
	public void setGui(Gui g) {
		gui = g;
	}
	public void setEnvironment(IEnvironment iEnv){
		env = iEnv;
	}
	
	public void itemStateChanged(PointListEvent pEvent) {
		pList = pEvent.getPointList();
		PointListRenderer plr = new PointListRenderer();
		plr.setPl(pList);
		List<IRenderer> renderer = new ArrayList<IRenderer>();
		renderer.add(plr);
		gui.getDrawArea().setDrawables(renderer);
	}

	public void openActionPerformed(ActionEvent e) {
		pList = openFile(gui);
		env.setPointList(pList);
	}

	public void handleException(Throwable t) {
		System.err.println("--------- UNCAUGHT EXCEPTION ---------");
		t.printStackTrace(System.err);
	}
	
	private  List<Point> openFile(Gui g){
		java.util.List<Point> pal = new ArrayList<Point>();
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "CSV Dateien", "csv");
		fc.setFileFilter(filter);
		int retVal = fc.showOpenDialog(g.getMainFrame());
		if (retVal == JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			OpenFile of = new OpenFile(file);
			pal = of.getValues();
		}	
		return Collections.unmodifiableList(pal);
	}

	public void getItemInfoMouseClickPerformed(MouseEvent e) {
		java.awt.Point point = new java.awt.Point(e.getPoint());
		if (pList!= null){
			if (env.inCircle(point) != null){
				System.out.println("Treffer");
				gui.getInfoPanel().setNewInfos(env.inCircle(point));
			} else System.out.println(" kein Treffer");
		}
		
	}

}
