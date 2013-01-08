package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import stadium.environment.Point;

public class OpenFile {

	private ArrayList<Point> plist;
	private File file;
	
	public OpenFile(File fil){
		plist = new ArrayList<Point>();
		file = fil;
	}
	public ArrayList<Point> getValues() {
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
				plist.add(point);
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
		return plist;
	}
}

