package be.uantwerpen.systemY.server;

import java.rmi.RemoteException;
import java.util.HashMap;

import be.uantwerpen.systemY.fileSystem.FileManager;
import be.uantwerpen.systemY.networkservices.Networkinterface;
import be.uantwerpen.systemY.shared.Node;

public class Server
{
	private static final String version = "v0.2";
	private Terminal terminal;
	private Networkinterface networkinterface;
	private FileManager fileManager;
	private NodeManager nodeManager;
	@SuppressWarnings("unused")
	private DiscoveryManager discoveryManager;
	private FailureManager failureManager;
	private String serverIP;
	
	/**
	 * Creates the Server Object.
	 * @throws RemoteException
	 */
	public Server(String networkIP, int networkPort, String multicastIP, int multicastPort) throws RemoteException
	{		
		this.serverIP = networkIP;
		
		//setup terminal
		terminal = new Terminal(this);
		
		printTerminal("SystemY 2014 Server " + version + " - Created by: Thomas Huybrechts, Quinten Van Hasselt, Arthur Janssens, Dries Blontrock");
		printTerminalInfo("Server starting...");
		
		nodeManager = new NodeManager();
		fileManager = new FileManager();
		failureManager = new FailureManager(this);
		
		//setup networkinterface
		networkinterface = new Networkinterface(networkIP, networkPort, multicastIP, multicastPort);
		
		//setup node lifecycle services
		discoveryManager = new DiscoveryManager(networkinterface.getMulticastObserver(), this);
		
		//start networkinterface
		if(startupProcedure())
		{
			printTerminalInfo("Server ready.");
		}
		else
		{
			printTerminalError("Server not fully operational, resolve the issue and reboot the server.");
		}
		
		activateTerminal();
	}
	
	/**
	 * Adds a Node to the NodeList.
	 * @param hostname	String
	 * @param ipAddress	ip in String format
	 * @return boolean True if successful, false otherwise
	 */
	public boolean addNode(String hostname, String ipAddress)
	{
		return nodeManager.addNode(hostname, ipAddress);
	}
	
	/**
	 * Deletes a node from the NodeList.
	 * @param hostname	String
	 * @return	boolean True if successful, false otherwise
	 */
	public boolean delNode(String hostname)
	{
		return nodeManager.delNode(hostname);
	}
	
	/**
	 * Loads a Nodelist from file.
	 * @param fileLocation	location of the file in String format
	 * @return	boolean True if successful, false otherwise
	 */
	public boolean loadNodeList(String fileLocation)
	{
		Object loadedFile = fileManager.loadXMLFile(NodeList.class, fileLocation);
		
		if(loadedFile != null)
		{
			try
			{
				nodeManager.setNodeList((NodeList)loadedFile);
			}
			catch(ClassCastException e)
			{
				return false;
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Saves a node to file.
	 * @param fileLocation	location in String format
	 * @return	boolean True if successful, false otherwise
	 */
	public boolean saveNodeList(String fileLocation)
	{
		return fileManager.saveXMLFile(nodeManager.getNodeList(), fileLocation);
	}
	
	/**
	 * Returns the ip where the file can be found.
	 * @param filename	filename in String format
	 * @return	String	ip
	 */
	public String getFileLocation(String filename)
	{
		return nodeManager.getNodeList().getFileLocation(filename);
	}
	
	/**
	 * Returns the HashMap of the NodeList.
	 * @return HashMap<Integer, Node>
	 */
	public HashMap<Integer, Node> getNodeList()
	{
		return nodeManager.getNodes();
	}
	
	/**
	 * Get server IP.
	 * @return	server IP
	 */
	public String getServerIP()
	{
		return this.serverIP;
	}
	
	/**
	 * Get the size of the node list.
	 * @return	size of node list
	 */
	public int getNetworkSize()
	{
		return this.nodeManager.getNodeList().getNodeList().size();
	}
	
	public Node getNextNode(String hostname)
	{
		return this.nodeManager.getNextNode(hostname);
	}
	
	public Node getPrevNode(String hostname)
	{
		return this.nodeManager.getPrevNode(hostname);
	}
	
	/**
	 * Get the interface bound to a specific location.
	 * @param bindLocation	
	 * @return	(Object) inteface
	 */
	public Object getRMIInterface(String bindLocation)
	{
		return this.networkinterface.getRMIInterface(bindLocation);
	}
	
	public void nodeConnectionFailure(String hostname) 
	{
		this.failureManager.nodeConnectionFailure(hostname);
	}
	
	/**
	 * Shuts down the server.
	 */
	public void exitServer()
	{
		System.exit(1);
	}
	
	/**
	 * Prints to the Terminal.
	 * @param message	String
	 */
	public void printTerminal(String message)
	{
		terminal.printTerminal(message);
	}
	
	/**
	 * Prints info message on the Terminal.
	 * @param message	String
	 */
	public void printTerminalInfo(String message)
	{
		terminal.printTerminalInfo(message);
	}
	
	/**
	 * Prints error message on the Terminal.
	 * @param message	String
	 */
	public void printTerminalError(String message)
	{
		terminal.printTerminalError(message);
	}
	
	/**
	 * start RMI-server and multicastservice
	 * @return boolean True if successful, false if failed
	 */
	private boolean startupProcedure()
	{
		//Start RMI-server
		networkinterface.startRMIServer();
		boolean RMIRunning = networkinterface.bindRMIServer(nodeManager, "NodeServer");
		
		//Start multicastservice
		if(networkinterface.setupMulticastservice())
		{
			networkinterface.runMulticastservice();
		}
		else
		{
			return false;
		}
		return RMIRunning;
	}
	
	/**
	 * Activates the terminal.
	 */
	private void activateTerminal()
	{
		terminal.activateTerminal();
	}
}