package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

public class InfoPanel extends JPanel{
	
	ArrayList<JComponent> attributes;
	public InfoPanel(){
		this.getPanelComponents();
	}

	private void getPanelComponents() {
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setMinimumSize(new Dimension(150, 200));
		setPreferredSize(new Dimension(150, 600));
		setLayout(new MigLayout("wrap 2","[grow][grow]","[grow][grow]"));
		attributes = new ArrayList<JComponent>();
		
	}
   
	public void setNewInfos(stadium.environment.Point p){
		if (p.getAttributes().size() % 2 != 1 && attributes.isEmpty()) {
			for (int i = 0; i < p.getAttributes().size(); i++) {
				if (i % 2 == 0){
					JLabel l = new JLabel(p.getAttributes().get(i)+":");
					attributes.add(l);
				} else if (i % 2 == 1){
					JTextField t = new JTextField(p.getAttributes().get(i));
					t.setEnabled(false);
					attributes.add(t);
				}
			}
			for (JComponent comp : attributes){
				add(comp);
			}
			validate();
		}else if (p.getAttributes().size() % 2 != 1){
			for (int i = 0; i < attributes.size();i++){
				if (attributes.get(i) instanceof JLabel){
					((JLabel)attributes.get(i)).setText(p.getAttributes().get(i)+":");
				} else if (attributes.get(i) instanceof JTextField){
					((JTextField)attributes.get(i)).setText(p.getAttributes().get(i));
				}
			}
			validate();
		} else System.out.println("error setting new infos");
		
	}
}
