package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Panel;
import java.awt.Color;

import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import stadium.environment.Point;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class GuiBuilder extends JFrame implements ActionListener{

	private JPanel contentPane;
	private JMenuItem mntmOpenFile;
	private Panel drawArea;
	private ArrayList<Point> pArrayList;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiBuilder frame = new GuiBuilder();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GuiBuilder() {
		pArrayList = new ArrayList<Point>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		drawArea = new Panel();
		drawArea.setBackground(Color.WHITE);
		contentPane.add(drawArea, BorderLayout.CENTER);
		
		JTree infoTree = new JTree();
		contentPane.add(infoTree, BorderLayout.EAST);
		
		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
		mntmOpenFile = new JMenuItem("Open File...");
		mntmOpenFile.addActionListener(this);
		menuBar.add(mntmOpenFile);
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mntmOpenFile){
			pArrayList = openFile();
		}
	}
	private ArrayList<Point> openFile(){
		ArrayList<Point> pal = new ArrayList<Point>();
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "CSV Dateien", "csv");
		fc.setFileFilter(filter);
		int retVal = fc.showOpenDialog(GuiBuilder.this);
		if (retVal == JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			BufferedReader br = null;
			
			try {
				br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				while (line != null){
					StringTokenizer strTok = new StringTokenizer(line, ";");
					int x = Integer.parseInt(strTok.nextToken());
					int y = Integer.parseInt(strTok.nextToken());
					String type = strTok.nextToken(); 
					int flow = Integer.parseInt(strTok.nextToken());
					int capacity = Integer.parseInt(strTok.nextToken());
					Point point = new Point(x, y, flow, capacity, type);
					pal.add(point);
					line = br.readLine();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			try{
				   br.close(); 
			} catch (Exception e){
				e.printStackTrace();	
				}
		}	
		return pal;
	}
	
}
