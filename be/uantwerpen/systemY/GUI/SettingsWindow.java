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
import javax.swing.JTextField;

public class SettingsWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private ClientGUI clientGUI;
	private JLabel hostnameLabel, ipAddressLabel, filedirLabel;
	private JTextField hostnameText, ipAddressText, filedirText;
	private JButton okButton, cancelButton;
	
	private final Image settingsIcon = Toolkit.getDefaultToolkit().getImage("pictures/SystemYSettings.png");
	
	public SettingsWindow(ClientGUI clientGUI)
	{
		
		this.clientGUI = clientGUI;
		this.clientGUI.setEnabled(false);
		this.clientGUI.setFocusable(false);
		this.setLayout(null);
			
		Font font = new Font("Arial", Font.PLAIN, 12);
		
	
		okButton = new JButton("Ok");
		okButton.setFont(font);
		okButton.setBounds(50, 220, 80, 30);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setFont(font);
		cancelButton.setBounds(150, 220, 80, 30);
		
		// hostname label + textfield
		hostnameLabel = new JLabel("Hostname:");
		hostnameLabel.setFont(font);
		hostnameLabel.setBounds(16, 20, 73, 20);
		
		hostnameText = new JTextField();
		hostnameText.setFont(font);
		hostnameText.setBounds(20, 40, 240, 26);
		hostnameText.setEnabled(!clientGUI.getLoginStatus());
		hostnameText.setText(clientGUI.getClientHostname());
		
		// ip address label + textfield
		ipAddressLabel = new JLabel("IP address:");
		ipAddressLabel.setFont(font);
		ipAddressLabel.setBounds(16, 80, 78, 20);
		
		ipAddressText = new JTextField();
		ipAddressText.setFont(font);
		ipAddressText.setBounds(20, 100, 240, 26);
		ipAddressText.setEnabled(!clientGUI.getLoginStatus());
		ipAddressText.setText(clientGUI.getClientIP());
		
		// filedir label + textfield
		filedirLabel = new JLabel("File directory:");
		filedirLabel.setFont(font);
		filedirLabel.setBounds(16, 140, 91, 20);
		
		filedirText = new JTextField();
		filedirText.setFont(font);
		filedirText.setBounds(20, 160, 240, 26);
		filedirText.setEnabled(!clientGUI.getLoginStatus());
		filedirText.setText(clientGUI.getClientFiledir());

		//Set GUI
		this.add(okButton);
		this.add(cancelButton);

		this.add(hostnameLabel);
		this.add(hostnameText);
		this.add(ipAddressLabel);
		this.add(ipAddressText);
		this.add(filedirLabel);
		this.add(filedirText);
		
		//Actionlisteners
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		initialisation();
	}
	
	
	/**
	 * Initializes the work window
	 */
	private void initialisation()
	{
		this.setTitle("Settings");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setIconImage(settingsIcon);
		this.setSize(290, 300);
		this.setResizable(false);
		
		// set max and min size as each other
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
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JButton b = (JButton)e.getSource();
		if(b == okButton)
		{
			boolean hostOk = clientGUI.setClientHostname(hostnameText.getText());
			boolean ipOk = clientGUI.setClientIP(ipAddressText.getText());
			boolean filedirOk = clientGUI.setClientFiledir(filedirText.getText());

			clientGUI.setEnabled(true);
			clientGUI.setFocusable(true);
			this.dispose();
			//close
		}
		else if(b == cancelButton)
		{
			clientGUI.setEnabled(true);
			clientGUI.setFocusable(true);
			this.dispose();
			//close
		}
	}
	
}
