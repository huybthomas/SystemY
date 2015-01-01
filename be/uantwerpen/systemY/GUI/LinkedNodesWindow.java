package be.uantwerpen.systemY.GUI;

import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class LinkedNodesWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private ClientGUI clientGUI;
	private JButton okButton;
	private JLabel prevLabel, ownLabel, nextLabel;
	
	private final Image settingsIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYSettings.png"));
	
	
	public LinkedNodesWindow(ClientGUI clientGUI)
	{
		this.clientGUI = clientGUI;
		this.clientGUI.setEnabled(false);
		this.clientGUI.setFocusable(false);
		this.setLayout(null);
			
		Font font = new Font("Arial", Font.PLAIN, 12);
		
		okButton = new JButton("Ok");
		okButton.setFont(font);
		okButton.setBounds(172, 130, 50, 30);
		
		prevLabel = new JLabel("Prev: " + clientGUI.getPrevHostname() + " - HASH: " + clientGUI.getPrevHash());
		prevLabel.setFont(font);
		prevLabel.setBounds(20, 10, 360, 30);
		
		ownLabel = new JLabel("This: " + clientGUI.getOwnHostname() + " - HASH: " + clientGUI.getOwnHash());
		ownLabel.setFont(font);
		ownLabel.setBounds(20, 50, 360, 30);

		nextLabel = new JLabel("Next: " + clientGUI.getNextHostname() + " - HASH: " + clientGUI.getNextHash());
		nextLabel.setFont(font);
		nextLabel.setBounds(20, 90, 360, 30);
		
		//Set GUI
		this.add(okButton);

		this.add(prevLabel);
		this.add(ownLabel);
		this.add(nextLabel);

		//Actionlisteners
		okButton.addActionListener(this);
		
		initialisation();
	}
	
	private void initialisation()
	{
		this.setTitle("Link info");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(400, 200);
		this.setIconImage(settingsIcon);
		this.setResizable(false);
		this.setLocation(this.clientGUI.getLocation().x + (this.clientGUI.getWidth() - this.getWidth()) / 2, this.clientGUI.getLocation().y + (this.clientGUI.getHeight() - this.getHeight()) / 2);
		
		//set max and min size as each other
		this.setMaximumSize(getMinimumSize());
		this.setMinimumSize(getMaximumSize());

		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				clientGUI.setEnabled(true);
				clientGUI.setFocusable(true);
			}
		});	
		
		this.setVisible(true);
		this.setAlwaysOnTop(true);
	}
	
	/**
	 * Checks for events
	 * @param Actionevent	the action that is performed
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JButton b = (JButton)e.getSource();
		if(b == okButton)
		{
			clientGUI.setEnabled(true);
			clientGUI.setFocusable(true);
			this.dispose();
			//close
		}
	}
	
}
