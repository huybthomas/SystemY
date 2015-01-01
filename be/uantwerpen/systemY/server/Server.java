package be.uantwerpen.systemY.server;

import java.rmi.RemoteException;
import java.util.HashMap;

import be.uantwerpen.systemY.fileSystem.FileSystemManager;
import be.uantwerpen.systemY.networkservices.Networkinterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Server class is the main class of the name server in the SystemY project
 */
public class Server
{
	private static final String version = "v0.6";
	private Terminal terminal;
	private Networkinterface networkinterface;
	private FileSystemManager fileManager;
	private NodeManager nodeManager;
	@SuppressWarnings("unused")
	private DiscoveryManager discoveryManager;
	private FailureManager failureManager;
	private String serverIP;
	
	/**
	 * Creates the Server Object.
	 * @throws RemoteException
	 */
	public Server(boolean enableTerminal, String networkIP, int tcpPort, int rmiPort, String multicastIP, int multicastPort) throws RemoteException
	{		
		this.serverIP = networkIP;
		
		//setup terminal
		terminal = new Terminal(this);
		
		printTerminal("SystemY 2014 Server " + version + " - Created by: Thomas Huybrechts, Quinten Van Hasselt, Arthur Janssens, Dries Blontrock");
		printTerminalInfo("Server starting...");
		
		nodeManager = new NodeManager();
		fileManager = new FileSystemManager();
		failureManager = new FailureManager(this);
		
		//setup networkinterface
		networkinterface = new Networkinterface(networkIP, rmiPort, tcpPort, tcpPort, multicastIP, multicastPort);
		
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
		
		if(enableTerminal)
		{
			activateTerminal();
		}
	}
	
	/**
	 * Adds a Node to the NodeList.
	 * @param hostname	The name of the node you want to add.
	 * @param ipAddress	The ip address of the node you want to add.
	 * @return True if successful, false otherwise.
	 */
	public boolean addNode(String hostname, String ipAddress)
	{
		return nodeManager.addNode(hostname, ipAddress);
	}
	
	/**
	 * Deletes a node from the NodeList.
	 * @param hostname	The name of the node you want to remove.
	 * @return	True if successful, false otherwise.
	 */
	public boolean delNode(String hostname)
	{
		return nodeManager.delNode(hostname);
	}
	
	/**
	 * Clear all nodes from the list
	 */
	public void clearList()
	{
		nodeManager.clearList();
	}
	
	/**
	 * Loads a Nodelist from file.
	 * @param fileLocation	Location of the file in String format.
	 * @return True if successful, false otherwise.
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
	 * @param fileLocation	Location in String format.
	 * @return	True if successful, false otherwise.
	 */
	public boolean saveNodeList(String fileLocation)
	{
		return fileManager.saveXMLFile(nodeManager.getNodeList(), fileLocation);
	}
	
	/**
	 * Returns the node where the file can be found.
	 * @param filename	Filename in String format.
	 * @return	The fileowner.
	 */
	public Node getFileLocation(String filename)
	{
		return nodeManager.getNodeList().getFileLocation(filename);
	}
	
	/**
	 * Returns the HashMap of the NodeList.
	 * @return A map with all the nodes.
	 */
	public HashMap<Integer, Node> getNodeList()
	{
		return nodeManager.getNodes();
	}
	
	/**
	 * Get server IP.
	 * @return	The server IP.
	 */
	public String getServerIP()
	{
		return this.serverIP;
	}
	
	/**
	 * Get the size of the node list.
	 * @return	Size of node list.
	 */
	public int getNetworkSize()
	{
		return this.nodeManager.getNodeList().getNodeList().size();
	}
	
	/**
	 * Get the next node in the network for a given node hostname.
	 * @param hostname	The name of the host you want to know the next node of.
	 * @return	The next node of the given node.
	 */
	public Node getNextNode(String hostname)
	{
		return this.nodeManager.getNextNode(hostname);
	}
	
	/**
	 * Get the previous node in the network for a given node hostname.
	 * @param hostname	The name of the host you want to know the next node of.
	 * @return	The previous node of the given node.
	 */
	public Node getPrevNode(String hostname)
	{
		return this.nodeManager.getPrevNode(hostname);
	}
	
	/**
	 * Get the interface bound to a specific location.
	 * @param bindLocation	The location where the interface needs to be bound.
	 * @return	boolean
	 */
	public Object getRMIInterface(String bindLocation)
	{
		return this.networkinterface.getRMIInterface(bindLocation);
	}
	
	public Object getNodeLinkInterface(Node node)
	{
		return this.networkinterface.getRMIInterface("//" + node.getIpAddress() + "/NodeLinkManager_" + node.getHostname());
	}
	
	public Object getBootstrapInterface(Node node)
	{
		return this.networkinterface.getRMIInterface("//" + node.getIpAddress() + "/Bootstrap_" + node.getHostname());
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
	 * @param message	The message that needs to be printed.
	 */
	public void printTerminal(String message)
	{
		terminal.printTerminal(message);
	}
	
	/**
	 * Prints info message on the Terminal.
	 * @param message	The message that needs to be printed.
	 */
	public void printTerminalInfo(String message)
	{
		terminal.printTerminalInfo(message);
	}
	
	/**
	 * Prints error message on the Terminal.
	 * @param message	The error that needs to be printed.
	 */
	public void printTerminalError(String message)
	{
		terminal.printTerminalError(message);
	}
	
	/**
	 * start RMI-server and multicastservice
	 * @return boolean True if successful, false if failed.
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