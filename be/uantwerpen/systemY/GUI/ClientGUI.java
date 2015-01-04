package be.uantwerpen.systemY.GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;

import be.uantwerpen.systemY.client.Client;

/**
 * Class that implements the GUI of a client.
 */
public class ClientGUI extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static String version;
	private final Image SYIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemY.png"));
	private final Image loginIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYLogin.png"));
	private final Image logoutIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYLogout.png"));
	private final Image infoIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYInfo.png"));
	private final Image settingsIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYSettings.png"));
	private JPanel filePanel, layoutPanel;
	private JScrollPane filePane;
	private JFileChooser fileChooser;
	private JButton loginButton, logoutButton, infoButton, addFileButton, settingsButton;
	private JLabel statusLabel, logoPicture;
	private Client client;
	private FileListRowPane fileListRowPane;
	private ArrayList<String> networkFiles;
	private LogoutStatusWindow logoutStatusWindow;
	private ClientGUI clientGUI = this;
	
	/**
	 * Sets up the client interface.
	 * @param client	The client on which the GUI is working.
	 */
	public ClientGUI(Client client)
	{
		//Declarations
		this.client = client;
		
		version = client.getVersion();
		
		this.client.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				execute((String) object);
			}
		});
		
		this.setLayout(null);
			
		Font font = new Font("Arial", Font.PLAIN, 12);
		
		filePanel = new JPanel();
		filePanel.setBounds(12, 59, 488, 289);
		filePanel.setLayout(null);
		
		layoutPanel = new JPanel();
		layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
		
	    filePane = new JScrollPane();
	    filePane.setViewportView(layoutPanel);
	    filePane.setBounds(0, 0, 472, 289);
		filePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		filePane.setBorder(BorderFactory.createTitledBorder("Files"));
		filePane.setVisible(false);
		
		loginButton = new JButton(new ImageIcon(new ImageIcon(loginIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		loginButton.setBounds(12, 12, 50, 40);
		loginButton.setToolTipText("Login");
		
		logoutButton = new JButton(new ImageIcon(new ImageIcon(logoutIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		logoutButton.setBounds(67, 12, 50, 40);
		logoutButton.setEnabled(false);
		logoutButton.setToolTipText("Logout");
		
		infoButton = new JButton(new ImageIcon(new ImageIcon(infoIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		infoButton.setBounds(430, 12, 50, 40);
		infoButton.setToolTipText("Info");
		
		settingsButton = new JButton(new ImageIcon(new ImageIcon(settingsIcon).getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH)));
		settingsButton.setBounds(375, 12, 50, 40);
		settingsButton.setToolTipText("Settings");		
		
		fileChooser = new JFileChooser();
		UIManager.put("FileChooser.openDialogTitleText", "Select file to add to SystemY");
		UIManager.put("FileChooser.openButtonText", "Select");
		SwingUtilities.updateComponentTreeUI(fileChooser);
		
		addFileButton = new JButton("Add file");
		addFileButton.setFont(font);
		addFileButton.setBounds(378, 354, 102, 30);
		addFileButton.setEnabled(false);
		
		statusLabel = new JLabel("Ready, click 'Login' to start.");
		statusLabel.setFont(font);
		statusLabel.setBounds(9, 368, 360, 13);
		
		logoPicture = new JLabel(new ImageIcon(new ImageIcon(SYIcon).getImage().getScaledInstance(290, 290, Image.SCALE_SMOOTH)));
		logoPicture.setBounds(85, 2, 290, 290);
		
		//Set GUI
		this.add(filePanel);
		filePanel.add(logoPicture);
		filePanel.add(filePane);
		this.add(loginButton);
		this.add(logoutButton);
		this.add(infoButton);
		this.add(addFileButton);
		this.add(settingsButton);
		this.add(statusLabel);

		//Actionlisteners
		loginButton.addActionListener(this);
		logoutButton.addActionListener(this);
		infoButton.addActionListener(this);
		addFileButton.addActionListener(this);
		settingsButton.addActionListener(this);
		
		initialisation();	
	}
	
	/**
	 * Checks for events.
	 * @param e	The action that is performed.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(GridButton.class.isInstance(e.getSource()))
		{
			GridButton b = (GridButton)e.getSource();
			switch(b.getColumn())
			{
				case(0):
				{
					client.openFile(networkFiles.get(b.getRow()));
					break;
				}
				case(1):
				{
					int n = JOptionPane.showConfirmDialog(this, "Delete " + networkFiles.get(b.getRow()) + " from the network?" , "Are you sure?", JOptionPane.YES_NO_OPTION);
					if(n == 0)
						client.deleteFileFromNetwork(networkFiles.get(b.getRow()));
					break;
				}
				case(2):
				{
					int n = JOptionPane.showConfirmDialog(this, "Delete " + networkFiles.get(b.getRow()) + " locally?" , "Are you sure?", JOptionPane.YES_NO_OPTION);
					if(n == 0)
						client.deleteLocalFile(networkFiles.get(b.getRow()));
					break;
				}
				default:{break;}	//non-existing button
			}
		}
		else if(JButton.class.isInstance(e.getSource()))
		{
			JButton b = (JButton)e.getSource();
			if(b == loginButton)
			{
				this.updateStatusLabel("Logging into the system. Please wait...");
				client.loginSystem();
			}
			else if(b == logoutButton)
			{
				this.updateStatusLabel("Logging out the system. Please wait...");
				logoutStatusWindow = new LogoutStatusWindow(this);
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						client.logoutSystem();
					}
				}).start();
			}
			else if(b == addFileButton)
			{
				if(client.getSessionState())
				{
					int returnValue = fileChooser.showOpenDialog(this);
					
					if(returnValue == JFileChooser.APPROVE_OPTION)
					{
						client.importFile(fileChooser.getSelectedFile());
					}
				}
			}
			else if(b == infoButton)
			{
				LinkedNodesWindow l = new LinkedNodesWindow(this);
				client.TESTprintOwnerFiles();
			}
			else if (b == settingsButton) 
			{
				SettingsWindow s = new SettingsWindow(this);
			}
		}
	}
	
	/**
	 * Initializes the work window.
	 */
	private void initialisation()
	{
		this.setTitle("SystemY 2014 " + version + " - " + client.getHostname());
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setIconImage(SYIcon);
		this.setSize(500, 418);
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
						clientGUI.updateStatusLabel("Logging out the system. Please wait...");
						logoutStatusWindow = new LogoutStatusWindow(clientGUI);
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								client.exitSystem();
							}
						}).start();
					}
				}
			}
		});	
		this.setVisible(true);
	}
	
	/**
	 * Get all the files in that are in the network.
	 * @return	The list with all the files.
	 */
	public ArrayList<String> getNetworkFiles()
	{
		return client.getNetworkFiles();
	}
	
	/**
	 * Get the client's hostname.
	 * @return The client hostname.
	 */
	public String getClientHostname() 
	{
		return this.client.getHostname();
	}
	
	/**
	 * Set the client's hostname.
	 * @param The client hostname.
	 */
	public boolean setClientHostname(String hostname)
	{
		this.setTitle("SystemY 2014 " + ClientGUI.version + " - " + hostname);
		return this.client.setHostname(hostname);
	}
	
	/**
	 * Get the client's ip address.
	 * @return The IP address of the client.
	 */
	public String getClientIP()
	{
		return this.client.getIP();
	}
	
	/**
	 * Set the client's ip address.
	 * @param the IP address of the client.
	 */
	public boolean setClientIP(String ip)
	{
		return this.client.setIP(ip);
	}
	
	/**
	 * Request the location of the file directory.
	 * @return The location of the files.
	 */
	public String getClientFiledir()
	{
		return client.getDownloadLocation();
	}
	
	/**
	 * Set the location of the file directory.
	 * @param filedir	The location you want the directory to be.
	 * @return	True if successful, false otherwise.
	 */
	public boolean setClientFiledir(String filedir)
	{
		return client.setDownloadLocation(filedir);
	}
	
	/**
	 * Get the hostname of the previous node.
	 * @return	The name of the previous node.
	 */
	public String getPrevHostname()
	{
		return client.getPrevNode().getHostname();
	}
	
	/**
	 * Get the hostname of the node.
	 * @return	The name of the node.
	 */
	public String getOwnHostname()
	{
		return client.getHostname();
	}
	
	/**
	 * Get the hostname of the next node.
	 * @return	The name of the next node.
	 */
	public String getNextHostname()
	{
		return client.getNextNode().getHostname();
	}
	
	/**
	 * Get the hash value of the previous node.
	 * @return	The hash value of the previous node.
	 */
	public int getPrevHash()
	{
		return client.getPrevNode().getHash();
	}
	
	/**
	 * Get the hash value of the next node.
	 * @return	The hash value of the next node.
	 */
	public int getNextHash()
	{
		return client.getNextNode().getHash();
	}
	
	/**
	 * Get the hash value of the node.
	 * @return	The hash value of the node.
	 */
	public int getOwnHash()
	{
		return client.getThisNode().getHash();
	}
	
	/**
	 * Get whether the client is connected or not.
	 * @return Get the login status.
	 */
	public boolean getLoginStatus()
	{
		return client.getSessionState();
	}
	
	/**
	 * Updates the list of files on the GUI.
	 */
	private void updateFileList()
	{
		int y = 0;
		layoutPanel.removeAll();
		
		for(String f : networkFiles)
		{
			fileListRowPane = new FileListRowPane(this, f, y, client.canBeDeleted(f));
			layoutPanel.add(fileListRowPane);
			y++;
		} 
		layoutPanel.updateUI();
	}
	
	/**
	 * Executes the command associated with the parameter.
	 * @param The text to be handled.
	 */
	private void execute(String text)
	{
		switch(text)
		{
			case("Login"):
			{
				loginButton.setEnabled(false);
				logoutButton.setEnabled(true);
				logoPicture.setVisible(false);
				filePane.setVisible(true);
				addFileButton.setEnabled(true);
				updateStatusLabel("System ready.");
				break;
			}
			case("LoginFailed"):
			{
				addFileButton.setEnabled(false);
				networkFiles = new ArrayList<String>();
				updateFileList();
				logoPicture.setVisible(true);
				filePane.setVisible(false);
				JOptionPane pane = new JOptionPane("Login Failed.\n\nAn error has occured during the login procedure.\n", JOptionPane.ERROR_MESSAGE);
				JDialog dialog = pane.createDialog("Error");
				dialog.setIconImage(SYIcon);	
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				updateStatusLabel("An error has occurred during login. The system may malfunction.");
				break;
			}
			case("LogoutFailed"):
			{
				JOptionPane pane = new JOptionPane("Logout Failed.\n\nAn error has occured during the logout procedure.\n", JOptionPane.ERROR_MESSAGE);
				JDialog dialog = pane.createDialog("Error");
				dialog.setIconImage(SYIcon);	
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				updateStatusLabel("An error has occurred during logout.");
				break;
			}
			case("Logout"):
			{
				loginButton.setEnabled(true);
				logoutButton.setEnabled(false);
				addFileButton.setEnabled(false);
				networkFiles = new ArrayList<String>();
				updateFileList();
				logoPicture.setVisible(true);
				filePane.setVisible(false);
				this.setEnabled(true);
				this.setFocusable(true);
				logoutStatusWindow.dispose();
				updateStatusLabel("Ready, click 'Login' to start.");
				break;
			}
			case("ConnectionFailure"):
			{
				JOptionPane pane = new JOptionPane("Connection lost to server.\n", JOptionPane.WARNING_MESSAGE);
				JDialog dialog = pane.createDialog("Warning");
				dialog.setIconImage(SYIcon);	
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				networkFiles = new ArrayList<String>();
				updateFileList();
				logoPicture.setVisible(true);
				filePane.setVisible(false);
				loginButton.setEnabled(true);
				logoutButton.setEnabled(false);
				updateStatusLabel("Connection lost to server.");
				break;
			}
			case("UpdateNetworkFiles"):
			{
				networkFiles = client.getNetworkFiles();
				updateFileList();
				break;
			}
			case("WaitingForDownloads"):
			{
				logoutStatusWindow.setStatus("Finishing downloads...");
				break;
			}
			case("DownloadsReady"):
			{
				logoutStatusWindow.setStatus("Downloads finished.");
				break;
			}
			case("WaitingForAgents"):
			{
				logoutStatusWindow.setStatus("Waiting for agents...");
				break;
			}
			case("AgentsReady"):
			{
				logoutStatusWindow.setStatus("Agents finished.");
				break;
			}
			case("WaitingForHostedDownloads"):
			{
				logoutStatusWindow.setStatus("Waiting for hosted downloads...");
				break;
			}
			case("HostedDownloadsReady"):
			{
				logoutStatusWindow.setStatus("Hosted downloads finished.");
				break;
			}
		}
	}
	
	/**
	 * Forces the client to logout.
	 */
	public void skipLogoutStep()
	{
		client.forceShutdown();
	}
	
	/**
	 * Update the label on the logout menu.
	 * @param message	The message that needs to be displayed.
	 */
	private void updateStatusLabel(String message)
	{
		this.statusLabel.setText(message);
		this.statusLabel.updateUI();
	}
	
	/**
	 * Closes the logout window.
	 */
	public void disposeLogoutStatusWindow() 
	{
		logoutStatusWindow.dispose();
	}
}