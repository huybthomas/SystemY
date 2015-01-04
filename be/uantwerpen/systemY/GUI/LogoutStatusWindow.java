package be.uantwerpen.systemY.GUI;

import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

public class LogoutStatusWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private JProgressBar progressBar;
	
	private ClientGUI clientGUI;
	private JButton skipButton;
	private JScrollPane jsp;
	private JPanel textPane;
	
	private final Image SYIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pictures/SystemYSettings.png"));
	
	/**
	 * Create a JFrame that contains info on the logout procedure.
	 * @param clientGUI The GUI that created it.
	 */
	public LogoutStatusWindow(ClientGUI clientGUI)
	{
		this.clientGUI = clientGUI;
		this.clientGUI.setEnabled(false);
		this.clientGUI.setFocusable(false);
		this.setLayout(null);
		
		Font font = new Font("Arial", Font.PLAIN, 14);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setBounds(15, 100, 280, 20);
		
		skipButton = new JButton("Skip");
		skipButton.setFont(font);
		skipButton.setBounds(305, 95, 80, 30);
		
		textPane = new JPanel();
		textPane.setLayout(new BoxLayout(textPane, BoxLayout.PAGE_AXIS));
		textPane.setAlignmentY(LEFT_ALIGNMENT);
		
		JLabel txt = new JLabel();
		textPane.add(txt);
		
		jsp = new JScrollPane();
		jsp.setViewportView(textPane);
		jsp.setBounds(20, 10, 360, 80);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setBorder(BorderFactory.createTitledBorder("Logging Out..."));

		//Set GUI
		this.add(skipButton);
		this.add(jsp);
		this.add(progressBar);
		
		//Actionlisteners
		skipButton.addActionListener(this);
		
		initialisation();
	}
	
	/**
	 * Initialise the JFrame.
	 */
	private void initialisation()
	{
		this.setTitle("Logout status");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setIconImage(SYIcon);
		this.setSize(400, 180);
		this.setResizable(false);
		this.setLocation(this.clientGUI.getLocation().x + (this.clientGUI.getWidth() - this.getWidth()) / 2, this.clientGUI.getLocation().y + (this.clientGUI.getHeight() - this.getHeight()) / 2);

		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				JOptionPane pane = new JOptionPane("Are you sure you want to Force Logout?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
				JDialog dialog = pane.createDialog("Force Logout");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				if(pane.getValue() != null)
				{
					if(((Integer)pane.getValue()).intValue() == JOptionPane.YES_OPTION)
					{
						clientGUI.setEnabled(true);
						clientGUI.setFocusable(true);
						clientGUI.disposeLogoutStatusWindow();
					}
				}
			}
		});	
		
		this.setVisible(true);
		this.setAlwaysOnTop(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JButton b = (JButton)e.getSource();
		if(b == skipButton)
		{
			clientGUI.skipLogoutStep();
		}
	}
	
	/**
	 * Sets the status of text in the JFrame.
	 * @param message
	 */
	public void setStatus(String message)
	{
		textPane.add(new JLabel(message));
		textPane.updateUI();
	}
}