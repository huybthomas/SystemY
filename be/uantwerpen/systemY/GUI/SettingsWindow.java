package be.uantwerpen.systemY.GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Class that implements the popup window for settings.
 */
public class SettingsWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private ClientGUI clientGUI;
	private JLabel hostnameLabel, ipAddressLabel, filedirLabel;
	private JTextField hostnameText, ipAddressText, filedirText;
	private JButton okButton, cancelButton, fileDirButton;
	private JFileChooser dirChooser;
	
	
	private final Image settingsIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYSettings.png"));
	
	/**
	 * Sets up the settingswindow object
	 * @param ClientGUI		the ClientGUI that is already running
	 */
	public SettingsWindow(ClientGUI clientGUI)
	{
		this.clientGUI = clientGUI;
		this.clientGUI.setEnabled(false);
		this.clientGUI.setFocusable(false);
		this.setLayout(null);
			
		Font font = new Font("Arial", Font.PLAIN, 12);
		
		okButton = new JButton("Ok");
		okButton.setFont(font);
		okButton.setBounds(50, 270, 80, 30);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setFont(font);
		cancelButton.setBounds(150, 270, 80, 30);
		
		//Hostname label + textfield
		hostnameLabel = new JLabel("Hostname:");
		hostnameLabel.setFont(font);
		hostnameLabel.setBounds(18, 20, 73, 20);
		
		hostnameText = new JTextField();
		hostnameText.setFont(font);
		hostnameText.setBounds(20, 40, 240, 26);
		hostnameText.setEnabled(!clientGUI.getLoginStatus());
		hostnameText.setText(clientGUI.getClientHostname());
		
		//Ip address label + textfield
		ipAddressLabel = new JLabel("IP address:");
		ipAddressLabel.setFont(font);
		ipAddressLabel.setBounds(18, 80, 78, 20);
		
		ipAddressText = new JTextField();
		ipAddressText.setFont(font);
		ipAddressText.setBounds(20, 100, 240, 26);
		ipAddressText.setEnabled(!clientGUI.getLoginStatus());
		ipAddressText.setText(clientGUI.getClientIP());
		
		//Filedir label + textfield
		filedirLabel = new JLabel("File directory:");
		filedirLabel.setFont(font);
		filedirLabel.setBounds(18, 140, 91, 20);
		
		filedirText = new JTextField();
		filedirText.setFont(font);
		filedirText.setBounds(20, 160, 240, 26);
		filedirText.setEnabled(false);
		filedirText.setDisabledTextColor(Color.DARK_GRAY);
		filedirText.setText(clientGUI.getClientFiledir());
		
		dirChooser = new JFileChooser();
		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dirChooser.setCurrentDirectory(new File(clientGUI.getClientFiledir()));
		UIManager.put("FileChooser.openDialogTitleText", "Select the SystemY file directory");
		UIManager.put("FileChooser.openButtonText", "Select");
		SwingUtilities.updateComponentTreeUI(dirChooser);
		
		fileDirButton = new JButton("Select");
		fileDirButton.setFont(font);
		fileDirButton.setBounds(180, 190, 80, 30);
		fileDirButton.setEnabled(true);

		//Set GUI
		this.add(okButton);
		this.add(cancelButton);
		this.add(fileDirButton);

		this.add(hostnameLabel);
		this.add(hostnameText);
		this.add(ipAddressLabel);
		this.add(ipAddressText);
		this.add(filedirLabel);
		this.add(filedirText);
		
		//Actionlisteners
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		fileDirButton.addActionListener(this);
		
		initialisation();
	}
	
	
	/**
	 * Initializes the settings window
	 */
	private void initialisation()
	{
		this.setTitle("Settings");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setIconImage(settingsIcon);
		this.setSize(290, 350);
		this.setResizable(false);
		this.setLocation(this.clientGUI.getLocation().x + (this.clientGUI.getWidth() - this.getWidth()) / 2, this.clientGUI.getLocation().y + (this.clientGUI.getHeight() - this.getHeight()) / 2);
		
		//Set max and min size as each other
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
			if(!clientGUI.getLoginStatus())
			{
				boolean hostOk = clientGUI.setClientHostname(hostnameText.getText());
				boolean ipOk = clientGUI.setClientIP(ipAddressText.getText());
				boolean filedirOk = clientGUI.setClientFiledir(filedirText.getText());
				
				if(hostOk && ipOk && filedirOk)
				{
					clientGUI.setEnabled(true);
					clientGUI.setFocusable(true);
					this.dispose();
					//close
				}
				else
				{
					String errorMessage = new String("The following settings are incorrect:\n");
					
					if(!hostOk)
					{
						errorMessage = errorMessage + " - The host id is invalid.\n";
					}
					
					if(!ipOk)
					{
						errorMessage = errorMessage + " - The ip address is invalid.\n";
					}
					
					if(!filedirOk)
					{
						errorMessage = errorMessage + " - The file directory is invalid.\n";
					}
					
					JOptionPane pane = new JOptionPane(errorMessage, JOptionPane.ERROR_MESSAGE);
					JDialog dialog = pane.createDialog("Error");
					dialog.setIconImage(settingsIcon);	
					dialog.setAlwaysOnTop(true);
					dialog.setVisible(true);
				}
			}
			else
			{
				clientGUI.setEnabled(true);
				clientGUI.setFocusable(true);
				this.dispose();
				//close
			}
		}
		else if(b == cancelButton)
		{
			clientGUI.setEnabled(true);
			clientGUI.setFocusable(true);
			this.dispose();
			//close
		}
		else if(b == fileDirButton)
		{
			int returnValue = dirChooser.showOpenDialog(this);
			
			if(returnValue == JFileChooser.APPROVE_OPTION)
			{
				File file = dirChooser.getSelectedFile();
				
				if(file.isDirectory())
				{
					clientGUI.setClientFiledir(file.getAbsolutePath());
					filedirText.setText(clientGUI.getClientFiledir());
					filedirText.updateUI();
				}
			}
		}
	}
	
}
