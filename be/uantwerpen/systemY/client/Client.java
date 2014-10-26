package be.uantwerpen.systemY.client;

import java.rmi.Naming;
import java.rmi.RemoteException;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.networkservices.Networkinterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Client class is the main class of the network nodes in the SystemY project
 */
public class Client
{
	private static final String version = "v0.3";
	private Terminal terminal;
	private NodeLinkManager nodeLinkManager;
	private BootstrapManager bootstrap;
	@SuppressWarnings("unused")
	private DiscoveryManager discovery;
	private ShutdownManager shutdownM;
	private FailureManager failure;
	private Networkinterface iFace;
	private boolean activeSession;
	
	/**
	 * Creates the Client Object.
	 * @param boolean	enableTerminal	make client boot along with a terminal window
	 * @param String	hostname	the client's hostname
	 * @param String	networkIP	the client's ip address
	 * @param int	rmiPort		the port for the remote method invocation calls
	 * @param String	multicastIP	the ip for multicasts
	 * @param int	multicastPort	the port for multicasts
	 * @throws RemoteException
	 */
	public Client(boolean enableTerminal, String hostname, String networkIP, int rmiPort, String multicastIP, int multicastPort) throws RemoteException
	{
		activeSession = false;
		
		//setup terminal
		terminal = new Terminal(this);
		
		//setup networkinterface
		iFace = new Networkinterface(networkIP, rmiPort, multicastIP, multicastPort);
		
		bootstrap = new BootstrapManager(this);
		discovery = new DiscoveryManager(this.iFace.getMulticastObserver(), this);
		shutdownM = new ShutdownManager(this);
		failure = new FailureManager(this);
		
		if(!setupServices())
		{
			System.err.println("System not fully operational. Instability issues can occur. Resolve the issue and reboot the system.");
		}
		
		nodeLinkManager = new NodeLinkManager(new Node(hostname, networkIP));
		
		if(enableTerminal)
		{
			printTerminal("SystemY 2014 Client " + version + " - Created by: Thomas Huybrechts, Arthur Janssens, Quinten Van Hasselt, Dries Blontrock");
			terminal.activateTerminal();
		}
	}
	
	/**
	 * Make client login to SystemY
	 * @return boolean	True if success, false if not
	 */
	public boolean loginSystem()
	{
		if(!activeSession)	
		{
			if(!bootstrap.startBootstrap())
			{
				printTerminalError("Login failed! Bootstrap didn't launch correctly.");
				return false;
			}
		}
		else
		{
			printTerminalError("Already an active session running.");
			return false;
		}
		return true;
	}
	
	/**
	 * Make client logout of SystemY
	 * @return boolean	True if success, false if not
	 */
	public boolean logoutSystem()
	{
		if(activeSession)
		{
			activeSession = false;
			if(!shutdownM.shutdown())
			{
				printTerminalError("Client didn't logout correctly out of the system.");
				return false;
			}
		}
		else
		{
			iFace.unbindRMIServer("Bootstrap_" + getHostname());
			printTerminalError("No active session running.");
			return false;
		}
		return true;
	}
	
	public void setSessionState(boolean state)
	{
		this.activeSession = state;
	}
	
	public boolean getSessionState()
	{
		return this.activeSession;
	}
	
	/**
	 * Start RMI and discovery multicast service
	 * @return boolean	True if success, false if not
	 */
	public boolean runServices()
	{
		//Start RMI-server
		if(!iFace.bindRMIServer(this.nodeLinkManager, "NodeLinkManager_" + getHostname()))
		{
			return false;
		}
		
		//Start discovery multicastservice
		iFace.runMulticastservice();
				
		return true;
	}
	
	/**
	 * Stop multicast and rmi services
	 * @return boolean	True if success, false if not
	 */
	public boolean stopServices()
	{
		activeSession = false;
		
		//Stop discovery multicastservice
		iFace.stopMulticastservice();
				
		//Stop RMI-server
		if(!iFace.unbindRMIServer("NodeLinkManager_" + getHostname()))
		{
			return false;
		}

		return true;
	}
	
	/**
	 * Send multicast to network
	 * @param byte[] 	message
	 * @return boolean	True if success, false if not
	 */
	public boolean sendMulticast(byte[] message)
	{
		return iFace.sendMulticast(message);
	}
	
	/**
	 * Closes the running session and shuts down the client.
	 */
	public void exitSystem()
	{
		if(activeSession)
		{
			logoutSystem();
		}
		System.exit(1);
	}
	
	/**
	 * Set the ip of the nameserver
	 * @param String	ip
	 */
	public void setServerIP(String ip)
	{
		this.nodeLinkManager.setServerIP(ip);
	}
	
	/**
	 * Get the ip of the nameserver
	 * @return String	ip
	 */
	public String getServerIP()
	{
		return this.nodeLinkManager.getServerIP();
	}
	
	/**
	 * Set the hostname of this node
	 * @param String	name
	 */
	public boolean setHostname(String name)
	{
		if(!activeSession)
		{
			this.nodeLinkManager.setMyHostname(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Get the hostname of this node
	 * @return String	name
	 */
	public String getHostname()
	{
		return this.nodeLinkManager.getMyHostname();
	}
	
	/**
	 * Get the ip address of this node
	 * @return String	ip
	 */
	public String getIP()
	{
		return this.nodeLinkManager.getMyIP();
	}
	
	/**
	 * Set the ip address of this node
	 * @param String	ip
	 */
	public boolean setIP(String ip)
	{
		if(!activeSession)
		{
			this.nodeLinkManager.setMyIP(ip);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Set the next and previous nodes of this node
	 * @param Node	prevNode
	 * @param Node	nextNode
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode)
	{
		this.nodeLinkManager.setLinkedNodes(prevNode, nextNode);
	}
	
	/**
	 * Let the nodeLinkManager add a newNode
	 * @param Node	newNode
	 */
	public Node updateLinks(Node newNode)
	{
		return this.nodeLinkManager.updateLinks(newNode);
	}
	
	/**
	 * Set the next node of this node
	 * @param Node	nextNode
	 */
	public boolean setNextNode(Node node)
	{
		if(node != null) 
		{
			try 
			{
				this.nodeLinkManager.setNext(node);
				return true;
			}
			catch(Exception e) 
			{
				System.err.println("NodeLinks exception:" + e.getMessage());
				return false;
			}
		} 
		else
		{
			return false;
		}
	}
	
	/**
	 * Set the previous node of this node
	 * @param Node	prevNode
	 */
	public boolean setPrevNode(Node node)
	{
		if(node != null) 
		{
			try 
			{
				this.nodeLinkManager.setPrev(node);
				return true;
			}
			catch(Exception e) 
			{
				System.err.println("NodeLinks exception:" + e.getMessage());
				return false;
			}
		} 
		else
		{
			return false;
		}
	}
	
	/**
	 * Get the next node of this node
	 * @return Node	nextNode
	 */
	public Node getNextNode()
	{
		return this.nodeLinkManager.getNext();
	}
	
	/**
	 * Get the previous node of this node
	 * @return Node	prevNode
	 */
	public Node getPrevNode()
	{
		return this.nodeLinkManager.getPrev();
	}
	
	/**
	 * Bind object to bindlocation
	 * @param Object	object to bind
	 * @param String	bindName
	 * @return boolean	True if success, false if not
	 */
	public boolean bindRMIservice(Object object, String bindName)
	{
		return iFace.bindRMIServer(object, bindName);
	}
	
	/**
	 * Unbind bindlocation
	 * @param String	bindName
	 * @return boolean	True if success, false if not
	 */
	public boolean unbindRMIservice(String bindName)
	{
		return iFace.unbindRMIServer(bindName);
	}
	
	/**
	 * Get object from bindlocation
	 * @param String	bindName
	 * @return Object	the RMI object
	 */
	public Object getRMIInterface(String bindLocation)
	{
		return this.iFace.getRMIInterface(bindLocation);
	}
	
	/**
	 * Handles the failure of a node, returns true if the failure is handled correctly.
	 * @param String	hostname
	 * @return boolean	True if connection failure handled correctly, false if not
	 */
	public boolean nodeConnectionFailure(String hostname)
	{
		return this.failure.nodeConnectionFailure(hostname);
	}
	
	/**
	 * Stops the services of this client because the server failed.
	 */
	public void serverConnectionFailure()
	{
		this.failure.serverConnectionFailure();
	}
	
	/**
	 * Prints to the Terminal.
	 * @param String	message
	 */
	public void printTerminal(String message)
	{
		terminal.printTerminal(message);
	}
	
	/**
	 * Prints info message on the Terminal.
	 * @param String	message
	 */
	public void printTerminalInfo(String message)
	{
		terminal.printTerminalInfo(message);
	}
	
	/**
	 * Prints error message on the Terminal.
	 * @param String 	message
	 */
	public void printTerminalError(String message)
	{
		terminal.printTerminalError(message);
	}
	
	/**
	 * Returns the value of activeSession
	 * @return	boolean
	 */
	public boolean returnActiveSession()
	{
		return activeSession;
	}
	
	/**
	 * Pings the given node.
	 * @param Node		the node to be pinged.
	 * @return boolean	True if connection succeed, otherwise false.
	 */
	public boolean ping(Node node)
	{
		String bindLocation = "//" + node.getIpAddress() + "/NodeLinkManager_" + node.getHostname();;

		try 
		{
			@SuppressWarnings("unused")
			NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)this.getRMIInterface(bindLocation);		//If RMI succeed than the node is still online
		} 
		catch(Exception e)
		{
			System.err.println("NodeLinkManager exception: "+ e.getMessage());
			nodeConnectionFailure(node.getHostname());
			return false;
		}
		return true;
	}
	
	/**
	 * Start RMI and multicast services
	 * @return boolean	True if connection succeed, otherwise false.
	 */
	private boolean setupServices()
	{
		//Start RMI-server
		iFace.startRMIServer();
						
		//Start multicastservice
		if(!iFace.setupMulticastservice())
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * METHODE UNDER CONSTRUCTION
	 * Returns the ip of the given filename.
	 *
	 * @param filename 		name of the file to be found
	 * @return           	return ip if found. Null otherwise.
	 */
	@SuppressWarnings("unused")
	private String getFileLocation(String filename)
	{
		String bindLocation = "//" + this.getServerIP() + "/NodeServer";		
		String ip;
		try 
		{
			NodeManagerInterface iFace = (NodeManagerInterface) Naming.lookup(bindLocation);
			ip = iFace.getFileLocation(filename);
			System.out.println(ip);
			return ip;
		} 
		catch(Exception e)
		{
			System.err.println("FileServer exception: "+ e.getMessage());
			serverConnectionFailure();
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * TESTMETHODE
	 */
	public void TESTprintLinkedNodes()
	{
		System.out.println("Prev: " + this.nodeLinkManager.getPrev().getHostname() + " - HASH: " + this.nodeLinkManager.getPrev().getHash());
		System.out.println("This: " + this.nodeLinkManager.getMyHostname());
		System.out.println("Next: " + this.nodeLinkManager.getNext().getHostname() + " - HASH: " + this.nodeLinkManager.getNext().getHash());
	}
}
