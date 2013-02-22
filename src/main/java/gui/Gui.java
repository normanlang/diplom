package gui;

import interfaces.IController;
import interfaces.IEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import net.miginfocom.swing.MigLayout;
import stadium.environment.Environment;
import controller.Controller;

public class Gui implements ActionListener, MouseListener{

	private IController control;
	private IEnvironment iEnv;
	private JPanel contentPane = null;
	private JFrame mainFrame =null;
	private JMenuBar menuBar = null;
	private JMenuItem mntmOpenFile;
	private DrawArea da = null;
	private InfoPanel infoPanel = null;
	
	public Gui(Controller c, Environment e){
		control = c;
		iEnv = e;
		iEnv.addIListener(control);
		this.getMainFrame();
	}
	public Gui(){
		this.getMainFrame();
		this.getController();
	}
	
	public JFrame getMainFrame(){
		if (mainFrame == null){
			mainFrame = new JFrame();
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.setBounds(100, 100, 450, 300);
		    mainFrame.setJMenuBar(getMenubar());
		    mainFrame.setContentPane(getContentPane());
		    mainFrame.pack();
		    mainFrame.setVisible(true);
		}
		return mainFrame;
	}
	
	private JPanel getContentPane() {
		if (contentPane == null){
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new MigLayout("","[grow][grow]","[grow][grow]"));	
			//add everything to the contentpane
			contentPane.add(getDrawArea(), "push");
			contentPane.add(getInfoPanel(), "push, wrap");
		}
		return contentPane;	
	}
	private JMenuBar getMenubar() {
		if (menuBar == null){
			menuBar = new JMenuBar();
			//menuepoints
			mntmOpenFile = new JMenuItem("Open File...");
			mntmOpenFile.addActionListener(this);
			menuBar.add(mntmOpenFile);
		}
		return menuBar;
	}
	
	public IController getController(){
		if (control == null){
			Controller defcont = new Controller();
			control = defcont;
			defcont.setGui(this);
			defcont.setEnvironment(getEnvironment());
			
		}
		return control;
	}
	
	public IEnvironment getEnvironment(){
		{
			if (iEnv == null)
			{
				iEnv = new Environment();
				iEnv.addIListener( getController());
			}
			return iEnv;
		}
	}
	
	public DrawArea getDrawArea(){
		if (da == null){
			da = new DrawArea();
			da.addMouseListener(this);
		}
		return da;
	}
	public InfoPanel getInfoPanel() {
		if (infoPanel == null){
			infoPanel = new InfoPanel();
		}
		return infoPanel;
	}
	//Listener-methods
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mntmOpenFile){
			try {
				control.openActionPerformed(e);
			} catch (NullPointerException e2) {
				e2.printStackTrace();
			}
		}
	}
	public void mouseClicked(MouseEvent e) {
		control.getItemInfoMouseClickPerformed(e);
	}
	
	
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
