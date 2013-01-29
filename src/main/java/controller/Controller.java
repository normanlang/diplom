package controller;

import gui.Gui;
import gui.GuiBuilder;

import interfaces.IEnvironment;
import interfaces.IController;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.omg.CORBA.Environment;

import stadium.environment.Point;
import stadium.environment.PointEvent;

public class Controller implements IController{

	private Gui gui;
	private IEnvironment env;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Controller c = new Controller();
					Gui g = new Gui(c);
					c.setGui(g);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Controller(IEnvironment e, Gui g){
		this.env = e;
		this.gui = g;
	}
	public Controller() {
	}
	public void setGui(Gui g) {
		this.gui = g;
	}
	public void setEnvironment(IEnvironment iEnv){
		this.env = iEnv;
	}
	
	public void itemStateChanged(PointEvent pEvent) {
		// TODO Auto-generated method stub
		
	}

	public void openActionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		openFile(gui);		
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
		int retVal = fc.showOpenDialog(g);
		if (retVal == JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			OpenFile of = new OpenFile(file);
			pal = of.getValues();
		}	
		return Collections.unmodifiableList(pal);
	}

}
