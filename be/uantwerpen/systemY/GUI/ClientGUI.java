package be.uantwerpen.systemY.GUI;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import be.uantwerpen.systemY.client.Client;

/**
 * Class that implements the GUI of a client.
 */
public class ClientGUI extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final String version = "v0.3";
	private final Image SYIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemY.png"));
	private final Image loginIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYLogin.png"));
	private final Image logoutIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYLogout.png"));
	private final Image infoIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYInfo.png"));
	private final Image settingsIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYSettings.png"));
	private JPanel filePanel;
	private JScrollPane filePane;
	private JList<String> fileList;
	private JButton loginButton, logoutButton, infoButton, fileActionButton, settingsButton;
	private JLabel statusLabel, logoPicture, testLabel, test2Label, test3Label;
	private Client client;
	
	/**
	 * Sets up the client interface
	 * @param Client	the client on which the GUI is working
	 */
	public ClientGUI(Client client)
	{
		//Declarations
		this.client = client;
		
		this.setLayout(null);
			
		Font font = new Font("Arial", Font.PLAIN, 12);
		
		filePanel = new JPanel();
		filePanel.setBounds(12, 59, 327, 289);
		filePanel.setLayout(null);
		
		filePane = new JScrollPane();
		filePane.setBounds(0, 0, 327, 289);
		filePane.setBorder(BorderFactory.createTitledBorder("Files"));
		
		fileList = new JList<String>();
		fileList.setFont(font);
		fileList.setBounds(0, 0, 327, 289);
		
		loginButton = new JButton(new ImageIcon(new ImageIcon(loginIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		loginButton.setBounds(12, 12, 50, 40);
		loginButton.setToolTipText("Login");
		
		logoutButton = new JButton(new ImageIcon(new ImageIcon(logoutIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		logoutButton.setBounds(67, 12, 50, 40);
		logoutButton.setEnabled(false);
		logoutButton.setToolTipText("Logout");
		
		infoButton = new JButton(new ImageIcon(new ImageIcon(infoIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		infoButton.setBounds(288, 12, 50, 40);
		infoButton.setToolTipText("Info");
		
		settingsButton = new JButton(new ImageIcon(new ImageIcon(settingsIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		settingsButton.setBounds(233, 12, 50, 40);
		settingsButton.setToolTipText("Settings");
		
		fileActionButton = new JButton("Select file...");
		fileActionButton.setFont(font);
		fileActionButton.setBounds(236, 354, 102, 30);
		fileActionButton.setEnabled(false);
		
		statusLabel = new JLabel("Ready, click 'Login' to start.");
		statusLabel.setFont(font);
		statusLabel.setBounds(9, 368, 216, 13);
		
		testLabel = new JLabel("TESTVALUES");	//VERWIJDEREN
		testLabel.setFont(font);
		testLabel.setBounds(9, 100, 290, 20);
		
		test2Label = new JLabel("TESTVALUES");
		test2Label.setFont(font);
		test2Label.setBounds(9, 120, 290, 20);
		
		test3Label = new JLabel("TESTVALUES");
		test3Label.setFont(font);
		test3Label.setBounds(9, 140, 290, 20);
		
		logoPicture = new JLabel(new ImageIcon(new ImageIcon(SYIcon).getImage().getScaledInstance(290, 290, Image.SCALE_SMOOTH)));
		logoPicture.setBounds(18, 0, 290, 290);
		
		//Set GUI
		this.add(filePanel);
		this.add(loginButton);
		this.add(logoutButton);
		this.add(infoButton);
		this.add(fileActionButton);
		this.add(settingsButton);
		this.add(statusLabel);
		
		filePanel.add(testLabel); 	//VERWIJDEREN
		filePanel.add(test2Label);
		filePanel.add(test3Label);
		
		//filePane.add(fileList);
		//filePanel.add(filePane);
		//filePanel.add(logoPicture);
		
		//Actionlisteners
		loginButton.addActionListener(this);
		logoutButton.addActionListener(this);
		infoButton.addActionListener(this);
		fileActionButton.addActionListener(this);
		settingsButton.addActionListener(this);
		
		initialisation();
	}
	
	/**
	 * Checks for events
	 * @param Actionevent	the action that is performed
	 */
	public void actionPerformed(ActionEvent e)
	{
		JButton b = (JButton)e.getSource();
		if(b == loginButton)
		{
			loginSystem();
		}
		else if(b == logoutButton)
		{
			logoutSystem();
		}
		else if(b == fileActionButton)
		{
			
		}
		else if(b == infoButton)
		{
			client.TESTprintLinkedNodes();
			testLabel.setText("Prev: " + client.getPrevNode().getHostname() + " - HASH: " + client.getPrevNode().getHash());
			test2Label.setText("This: " + client.getHostname());
			test3Label.setText("Next: " + client.getNextNode().getHostname() + " - HASH: " + client.getNextNode().getHash());
		}
		// SystemY venster moet nu op inactief staan tot het settings venster gesloten is.
		else if (b == settingsButton) 
		{
			SettingsWindow s = new SettingsWindow(this);
		}
	}
	
	/**
	 * Initializes the work window
	 */
	private void initialisation()
	{
		this.setTitle("SystemY 2014 " + version + " - " + client.getHostname());
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setIconImage(SYIcon);
		this.setSize(355, 418);
		this.setResizable(false);
		this.setLocationRelativeTo(null);				//Centralize the frame on the screen
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				JOptionPane pane = new JOptionPane("Are you sure you want to exit?\n\nLocal files on this system will not be available anymore on SystemY.\n", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
				JDialog dialog = pane.createDialog("Exit application");
				dialog.setIconImage(SYIcon);	
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				if(pane.getValue() != null)
				{
					if(((Integer)pane.getValue()).intValue() == JOptionPane.YES_OPTION)
					{
						client.exitSystem();
					}
				}
			}
		});	
		this.setVisible(true);
	}
	
	/**
	 * Get the client's hostname
	 * @return String	client hostname
	 */
	public String getClientHostname() 
	{
		return this.client.getHostname();
	}
	
	/**
	 * Set the client's hostname
	 * @param String	client hostname
	 */
	public boolean setClientHostname(String hostname)
	{
		return this.client.setHostname(hostname);
	}
	
	/**
	 * Get the client's ip address
	 * @return String	client ip address
	 */
	public String getClientIP()
	{
		return this.client.getIP();
	}
	
	/**
	 * Set the client's ip address
	 * @param String	client ip address
	 */
	public boolean setClientIP(String ip)
	{
		return this.client.setIP(ip);
	}
	
	// Still to be implemented for later use
	public String getClientFiledir()
	{
		return "C://Documents/default/";
	}
	// Still to be implemented for later use
	public boolean setClientFiledir(String filedir)
	{
		return false;
	}
	
	/**
	 * Get whether the client is connected or not.
	 * @return boolean	Get the login status
	 */
	// might need to be improved
	public boolean getLoginStatus()
	{
		return logoutButton.isEnabled();
	}
	
	/**
	 * Makes the client login
	 */
	private void loginSystem()
	{
		client.loginSystem();
		loginButton.setEnabled(false);
		logoutButton.setEnabled(true);
	}
	
	/**
	 * Makes the client logout
	 */
	private void logoutSystem()
	{
		client.logoutSystem();
		logoutButton.setEnabled(false);
		loginButton.setEnabled(true);
	}
}
