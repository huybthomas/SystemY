package be.uantwerpen.systemY.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Map.Entry;

import be.uantwerpen.systemY.shared.Node;
import be.uantwerpen.systemY.terminal.TerminalReader;

/**
 * Terminal class to interpret the input of the console and execute methods of the server class
 */
public class Terminal
{
	private Server server;
	private TerminalReader terminalReader;
	
	/**
	 * Creates the Terminal Object.
	 * @param server	The server on which you want to create the terminal.
	 */
	public Terminal(Server server)
	{
		this.server = server;
		
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
	 * @param message	The message you want to print.
	 */
	public void printTerminal(String message)
	{
		System.out.println(message);
	}
	
	/**
	 * Prints info message to the Terminal.
	 * @param message	The message you want to print.
	 */
	public void printTerminalInfo(String message)
	{
		Calendar calender = Calendar.getInstance();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		
		System.out.println("[INFO - " + timeFormat.format(calender.getTime()) + "] " + message);
	}
	
	/**
	 * Prints error message to the Terminal.
	 * @param message	The error you want to print.
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
	 * Executes a command.
	 * @param commandString	The command you want to execute.
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
						exitServer();
						break;
				case "addnode":
						if(commandString.split(" ", 3).length <= 2)
						{
							printTerminalInfo("Missing arguments! 'addNode {hostname} {ipAddress}'");
						}
						else
						{
							String hostname = commandString.split(" ", 3)[1];
							String ipAddress = commandString.split(" ", 3)[2];
							addNode(hostname, ipAddress);
						}	
						break;
				case "delnode":
						if(commandString.split(" ", 2).length <= 1)
						{
							printTerminalInfo("Missing arguments! 'delNode {hostname}'");
						}
						else
						{
							String hostname = commandString.split(" ", 2)[1];
							delNode(hostname);
						}
						break;
				case "shownodes":
						printNodeList();
						break;
				case "loadnodelist":
						if(commandString.split(" ", 2).length <= 1)
						{
							loadNodeList("data/nodeList.XML");
						}
						else
						{
							String fileLocation = commandString.split(" ", 2)[1];
							loadNodeList(fileLocation);
						}	
						break;
				case "savenodelist":
						if(commandString.split(" ", 2).length <= 1)
						{
							saveNodeList("data/nodeList.XML");
						}
						else
						{
							String fileLocation = commandString.split(" ", 2)[1];
							saveNodeList(fileLocation);
						}	
						break;
				case "clearnodes":
						clearNodeList();
						break;
				case "getfilelocation":
						if(commandString.split(" ", 2).length <= 1)
						{
							printTerminalInfo("Missing arguments! 'getFileLocation {filename}'");
						}
						else
						{
							printTerminalInfo("Command for debugpurpose only!");
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
	 * Adds a Node to the NodeList.
	 * @param hostname	The name of the node you want to add.
	 * @param ipAddress	The ip of the node you want to add.
	 * @return Boolean	True if successful, false otherwise.
	 */
	private boolean addNode(String hostname, String ipAddress)
	{
		if(server.addNode(hostname, ipAddress))
		{
			printTerminalInfo("Node: " + hostname + " - " + ipAddress + " added to nodelist.");
			return true;
		}
		else
		{
			printTerminalError("Node: " + hostname + " already exist!");
			return false;
		}
	}
	
	/**
	 * Deletes a Node from the NodeList.
	 * @param hostname	Name of the Node to be deleted.
	 * @return	boolean True if successful false otherwise.
	 */
	private boolean delNode(String hostname)
	{
		if(server.delNode(hostname))
		{
			printTerminalInfo("Node: " + hostname + " deleted from nodelist.");
			return true;
		}
		else
		{
			printTerminalError("Node: " + hostname + " not found!");
			return false;
		}
	}
	
	/**
	 * Loads the NodeList from a file.
	 * @param fileLocation	Location of the file to be read.
	 * @return	boolean 	True if successful, false otherwise.
	 */
	private boolean loadNodeList(String fileLocation)
	{
		if(server.loadNodeList(fileLocation))
		{
			printTerminalInfo("Nodelist loaded from: '" + fileLocation + "'.");
			return true;
		}
		else
		{
			printTerminalError("File doesn't exists or isn't compatible!");
			return false;
		}
	}
	
	/**
	 * Saves the NodeList to a file.
	 * @param fileLocation	Location of the file to be saved.
	 * @return	boolean		True if successful, false otherwise
	 */
	private boolean saveNodeList(String fileLocation)
	{
		if(server.saveNodeList(fileLocation))
		{
			printTerminalInfo("Nodelist saved.");
			return true;
		}
		else
		{
			printTerminalError("Nodelist not saved!");
			return false;
		}
	}
	
	/**
	 * Clear all nodes from the list
	 */
	private void clearNodeList()
	{
		server.clearList();
		printTerminalInfo("Nodelist cleared!");
	}
	
	/**
	 * Returns the ip where a file can be found.
	 * @param filename	The name of the file you want the location of.
	 * @return	boolean	True if successful, false otherwise.
	 */
	private boolean getFileLocation(String filename)
	{
		
		String fileLocation = server.getFileLocation(filename).getIpAddress();
		
		if(fileLocation != null)
		{
			printTerminalInfo("File: '" + filename + "' located on node: " + fileLocation);
			return true;
		}
		else
		{
			printTerminalError("There are no nodes in the nodelist.");
			return false;
		}
	}
	
	/**
	 * Shuts down the server.
	 */
	private void exitServer()
	{
		server.exitServer();
	}
	
	/**
	 * Prints the list of nodes in NodeList on the terminal.
	 */
	private void printNodeList()
	{
		printTerminal("Hostname\t\tIP-address");
		printTerminal("--------------------------------------");
		Iterator<Entry<Integer, Node>> iterator = server.getNodeList().entrySet().iterator();
		while(iterator.hasNext())
		{
			Node node = iterator.next().getValue();
			printTerminal(node.getHostname() + "\t\t\t" + node.getIpAddress());
		}
		printTerminal("");
	}
	
	/**
	 * Prints help for Terminal commands on the Terminal.
	 */
	private void help()
	{
		printTerminal("Available commands:");
		printTerminal("-------------------");
		printTerminal("'stop' : shutdown the server.");
		printTerminal("'addNode {hostname} {ipAddress}' : add an entry to the nodelist.");
		printTerminal("'delNode {hostname}' : delete an entry from the nodelist.");
		printTerminal("'showNodes' : show all entries from the nodelist.");
		printTerminal("'clearNodes' : clear all entries from the nodelist.");
		printTerminal("'loadNodeList [fileLocation]' : load the nodelist from the specified filelocation.");
		printTerminal("'saveNodeList [fileLocation]' : save the nodelist to the specified filelocation.");
		printTerminal("'help' / '?' : show all available commands.\n");
	}
}
