package be.uantwerpen.systemY.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import be.uantwerpen.systemY.terminal.TerminalReader;

/**
 * Terminal class to interpret the input of the console and execute methods of the client class
 */
public class Terminal
{
	private Client client;
	private TerminalReader terminalReader;
	
	/**
	 * Creates the Terminal Object.
	 * @param Client 	client
	 */
	public Terminal(Client client)
	{
		this.client = client;
		
		//Setup terminal reader
		terminalReader = new TerminalReader();
		
		terminalReader.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				executeCommand((String) object);
			}
		});
	}
	
	/**
	 * Prints a message to the Terminal.
	 * @param String	message
	 */
	public void printTerminal(String message)
	{
		System.out.println(message);
	}
	
	/**
	 * Prints info message to the Terminal.
	 * @param String	message
	 */
	public void printTerminalInfo(String message)
	{
		Calendar calender = Calendar.getInstance();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		
		System.out.println("[INFO - " + timeFormat.format(calender.getTime()) + "] " + message);
	}
	
	/**
	 * Prints error message to the Terminal.
	 * @param String	message
	 */
	public void printTerminalError(String message)
	{
		Calendar calender = Calendar.getInstance();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		
		System.out.println("[ERROR - " + timeFormat.format(calender.getTime()) + "] " + message);
	}
	
	/**
	 * Activates the terminal.
	 */
	public void activateTerminal()
	{
		new Thread(terminalReader).start();
	}
	
	/**
	 * Executes a command
	 * @param String	commandString	Command to be executed in String format
	 */
	private void executeCommand(String commandString)
	{
		commandString = commandString.trim();
		
		if(commandString != null && !commandString.equals(""))
		{
			String command = commandString.split(" ", 2)[0].toLowerCase();
			
			switch(command)
			{
				case "stop":
						exitClient();
						break;
				case "showhostinfo":
						getHostInfo();
						break;
				case "sethostname":
						if(commandString.split(" ", 2).length <= 1)
						{
							printTerminalInfo("Missing arguments! 'setHostName [hostname]'");
						}
						else
						{
							String hostname = commandString.split(" ", 2)[1];
							setHostname(hostname);
						}
						break;
				case "setip":
						if(commandString.split(" ", 2).length <= 1)
						{
							printTerminalInfo("Missing arguments! 'setIP [ipaddress]'");
						}
						else
						{
							String ip = commandString.split(" ", 2)[1];
							setIP(ip);
						}
						break;
				case "testlinks":
						testLinks();
						break;
				case "login":
						loginSystem();
						break;
				case "logout":
						logoutSystem();
						break;
				case "getfilelocation":
						if(commandString.split(" ", 2).length <= 1)
						{
							printTerminalInfo("Missing arguments! 'getFileLocation [filename]'");
						}
						else
						{
							printTerminalInfo("Command under construction!");
							String filename = commandString.split(" ", 2)[1];
							getFileLocation(filename);
						}
						break;
				case "help":
						help();
						break;
				case "?":
						help();
						break;
				default:
						printTerminalInfo("Command: '" + command + "' is not recognized.");
						break;
			}
		}
		activateTerminal();
	}
	
	/**
	 * METHOD STILL UNDER CONSTRUCTION
	 * Returns the ip where a file can be found.
	 * @param String	filename
	 * @return	boolean	True if successful, false otherwise
	 */
	private String getFileLocation(String filename)
	{
		return "";
	}
	
	/**
	 * Let client login to SystemY.
	 */
	private void loginSystem()
	{
		client.loginSystem();
	}
	
	/**
	 * Prints the host Information (hostname & ip address)
	 */
	private void getHostInfo()
	{
		printTerminal("host: " + client.getHostname() + " IP: " + client.getIP());
	}
	
	/**
	 * Change the host name.
	 * @param String	name
	 * @return boolean	True if successful, false if failed
	 */
	private boolean setHostname(String name)
	{
		if(client.setHostname(name))
		{
			printTerminalInfo("Hostname changed to: " + name);
			return true;
		}
		else
		{
			printTerminalError("Close the active session before changing the hostname.");
			return false;
		}
	}
	
	/**
	 * Change the host ip address.
	 * @param String	ip
	 * @return boolean	True if successful, false if failed
	 */
	private boolean setIP(String ip)
	{
		if(client.setIP(ip))
		{
			printTerminalInfo("IP address changed to: " + ip);
			return true;
		}
		else
		{
			printTerminalError("Close the active session before changing the ip address.");
			return false;
		}
	}
	
	private void testLinks()
	{
		boolean linksUp = true;
		
		if(client.getSessionState())
		{
			if(!client.ping(client.getNextNode()))
			{
				printTerminalInfo("The next link was broken and is now recoverd.");
				linksUp = false;
			}
			
			if(!client.ping(client.getPrevNode()))
			{
				printTerminalInfo("The previous link was broken and is now recoverd.");
				linksUp = false;
			}
			
			if(linksUp)
			{
				printTerminalInfo("All links are up.");
			}
		}
		else
		{
			printTerminalError("The node is not logged in to the network.");
		}
	}
	
	/**
	 * Let client logout of SystemY.
	 */
	private void logoutSystem()
	{
		client.logoutSystem();
	}
	
	/**
	 * Shuts down the client.
	 */
	private void exitClient()
	{
		client.exitSystem();
	}
	
	/**
	 * Prints help for Terminal commands on the Terminal.
	 */
	private void help()
	{
		printTerminal("Available commands:");
		printTerminal("-------------------");
		printTerminal("'login' : login to the netwerk.");
		printTerminal("'logout' : logout from the netwerk.");
		printTerminal("'setHostname' : change the hostname of the system.");
		printTerminal("'setIP' : change the ip address of the system.");
		printTerminal("'showHostInfo' : get info of the localhost.");
		printTerminal("'testLinks' : check the state of the linked nodes.");
		printTerminal("'stop' : shutdown the client.");
		printTerminal("'help' / '?' : show all available commands.\n");
	}
}
