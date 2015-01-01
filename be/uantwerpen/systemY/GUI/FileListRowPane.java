package be.uantwerpen.systemY.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class FileListRowPane extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private ClientGUI clientGUI;
	
	private GridBagConstraints constr;

	public FileListRowPane(ClientGUI clientGUI, String fileName, int y, boolean localExistence)
	{	
		this.clientGUI = clientGUI;
		
		setBorder(new LineBorder(Color.LIGHT_GRAY));
		this.setMaximumSize(new Dimension(470, 40));
		
		constr = new GridBagConstraints();
	    constr.ipady = 5;
	    constr.insets = new Insets(0,2,0,3); 			//Extern padding
	    		
		JLabel label = new JLabel(fileName + " "); 		//Extra space for better graphical display
    	label.setHorizontalTextPosition(SwingConstants.LEFT);
    	label.setPreferredSize(new Dimension(180,30));
    	label.setToolTipText(fileName);
    	
    	GridButton openButton = new GridButton(0,y);
    	openButton.setText("Open");
    	openButton.setToolTipText("Open " + fileName);
    	
    	GridButton deleteButton = new GridButton(1,y);
    	deleteButton.setText("Delete");
    	deleteButton.setBounds(0, 0, 50, 20);
    	deleteButton.setToolTipText("Delete " + fileName + " from SystemY");
    	
    	GridButton deleteLocalButton = new GridButton(2,y);
    	deleteLocalButton.setToolTipText("Delete " + fileName + " locally");
    	deleteLocalButton.setBounds(0, 0, 50, 20);
    	deleteLocalButton.setText("Delete Local");

        constr.gridx = 0;
        constr.ipadx = 5;
        this.add(label, constr);
        constr.ipadx = 0;
        constr.gridx = 1;
        this.add(openButton, constr);
        constr.gridx = 2;
        this.add(deleteButton, constr);
        constr.gridx = 3;
        if(!localExistence)
        {
        	deleteLocalButton.setEnabled(false);
        }
        this.add(deleteLocalButton, constr);
        
        openButton.addActionListener(clientGUI);
        deleteButton.addActionListener(clientGUI);
        deleteLocalButton.addActionListener(clientGUI);
	}	
}
