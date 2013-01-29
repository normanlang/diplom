package gui;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Panel;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import net.miginfocom.swing.MigLayout;
import controller.Controller;
import controller.OpenFile;
import stadium.environment.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Norman Langner
 * creates the gui for the simulation-environment
 */
public class GuiBuilder extends JPanel implements ActionListener{

	private JPanel contentPane;
	private JMenuItem mntmOpenFile;
	private JPanel drawArea;
	private List<Point> pointList;
	private JMenuBar menubar;
	private JTree infoTree;
	private Controller control;

	public GuiBuilder(Controller c){
		setController(c);
	}

	
	/**
	 * Create the JPanel
	 */
	public JPanel getContentPane() {
		pointList = new ArrayList<Point>();
		if (contentPane == null){
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new MigLayout("","[grow][grow]",""));
			drawArea = new JPanel();
			drawArea.setBackground(Color.WHITE);
			drawArea.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			drawArea.setMinimumSize(new Dimension(200, 200));
			infoTree = new JTree();		
			//add everything to the contentpane
			contentPane.add(drawArea, 	"grow");
			contentPane.add(infoTree, 	"growy");
		}
		return contentPane;	
	}
	
	public JMenuBar getMenubar() {
		if (menubar == null){
			menubar = new JMenuBar();
			//menuepoints
			mntmOpenFile = new JMenuItem("Open File...");
			mntmOpenFile.addActionListener(this);
			menubar.add(mntmOpenFile);
		}
		return menubar;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mntmOpenFile){
			try {
				control.openActionPerformed(e);
			} catch (NullPointerException e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private void setController (Controller c){
		control = c;
	}
}
