package be.uantwerpen.systemY.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.networkservices.Networkinterface;
import be.uantwerpen.systemY.shared.Node;

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
			System.err.println("System not fully operational. Instability issues can occure. Resolve the issue and reboot the system.");
		}
		
		nodeLinkManager = new NodeLinkManager(new Node(hostname, networkIP));
		
		if(enableTerminal)
		{
			printTerminal("SystemY 2014 Client " + version + " - Created by: Thomas Huybrechts, Arthur Janssens, Quinten Van Hasselt, Dries Blontrock");
			terminal.activateTerminal();
		}
	}
	
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
		activeSession = true;
		return true;
	}
	
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
	
	public boolean sendMulticast(byte[] message)
	{
		return iFace.sendMulticast(message);
	}
	
	/**
	 * Close the running session and shuts down the client.
	 */
	public void exitSystem()
	{
		if(activeSession)
		{
			logoutSystem();
		}
		System.exit(1);
	}
	
	public void setServerIP(String ip)
	{
		this.nodeLinkManager.setServerIP(ip);
	}
	
	public String getServerIP()
	{
		return this.nodeLinkManager.getServerIP();
	}
	
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
	
	public String getHostname()
	{
		return this.nodeLinkManager.getMyHostname();
	}
	
	public String getIP()
	{
		return this.nodeLinkManager.getMyIP();
	}
	
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
	
	public void setLinkedNodes(Node prevNode, Node nextNode)
	{
		this.nodeLinkManager.setLinkedNodes(prevNode, nextNode);
	}
	
	public Node updateLinks(Node newNode)
	{
		return this.nodeLinkManager.updateLinks(newNode);
	}
	
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
	
	public Node getNextNode()
	{
		return this.nodeLinkManager.getNext();
	}
	
	public Node getPrevNode()
	{
		return this.nodeLinkManager.getPrev();
	}
	
	public boolean bindRMIservice(Object object, String bindName)
	{
		return iFace.bindRMIServer(object, bindName);
	}
	
	public boolean unbindRMIservice(String bindName)
	{
		return iFace.unbindRMIServer(bindName);
	}
	
	public Object getRMIInterface(String bindLocation)
	{
		return this.iFace.getRMIInterface(bindLocation);
	}
	
	public boolean nodeConnectionFailure(String hostname)
	{
		return this.failure.nodeConnectionFailure(hostname);
	}
	
	public void serverConnectionFailure()
	{
		this.failure.serverConnectionFailure();
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
	 * Returns the value of activeSession
	 * @return	boolean
	 */
	public boolean returnActiveSession()
	{
		return activeSession;
	}
	
	/**
	 * Pings the given node.
	 *
	 * @param node		the node to be pinged.
	 * @return          true if connection succeed, otherwise false.
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
