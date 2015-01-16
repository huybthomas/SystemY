package be.uantwerpen.systemY.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Observable;
import java.util.Observer;

import be.uantwerpen.systemY.interfaces.BootstrapManagerInterface;
import be.uantwerpen.systemY.shared.Node;
import be.uantwerpen.systemY.timer.TimerService;

/**
 * Class that handles the bootstrap of a client.
 * @extends UnicastRemoteObject
 * @implements BootstrapManagerInterface
 */
public class BootstrapManager extends UnicastRemoteObject implements BootstrapManagerInterface
{
	private static final long serialVersionUID = 1L;
	private Client client;
	private boolean serverRespons, prevNodeRespons;
	private boolean firstNetworkNode;
	private TimerService timeOut;
	
	/**
	 * Create the BootstrapManager object.
	 * @param client	The client where the bootstrapmanager is summoned upon.
	 * @throws RemoteException
	 */
	public BootstrapManager(Client client) throws RemoteException
	{
		this.client = client;
		this.timeOut = new TimerService(3000);	//3 seconds
		
		timeOut.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				timeOutDetection();
			}
		});
	}
	
	/**
	 * Sets the links to the next and previous nodes of a client. This is the final step in the bootstrap.
	 * @param prevNode	The previous node, adjacent to this node.
	 * @param nextNode	The next node, adjacent to this node.
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode)
	{
		client.setLinkedNodes(prevNode, nextNode);
		prevNodeRespons = true;
		
		finishBootstrap();
	}
	
	/**
	 * Give the server's IP address and the network size to the new node.
	 * @param serverIP	The IP address of the server.
	 * @param networkSize	Amount of clients connected to the network.
	 */
	public void setNetwork(String serverIP, int networkSize)
	{
		client.setServerIP(serverIP);
		if(networkSize == 0)
		{
			Node thisNode = new Node(client.getHostname(), client.getIP());
			client.setLinkedNodes(thisNode, thisNode);
			firstNetworkNode = true;
		}
		serverRespons = true;
		
		finishBootstrap();
	}
	
	/**
	 * Implements the bootstrap service: Initialization of the bootstrap. Reset of the node links and the server IP address. Finally, a discovery multicast is sent.
	 * @return boolean	True if the function succeeded without errors, false if not.
	 */
	public boolean startBootstrap()
	{
		//Initialize bootstrap
		serverRespons = false;
		prevNodeRespons = false;
		firstNetworkNode = false;
		
		//Reset the node links
		client.setNextNode(client.getThisNode());
		client.setPrevNode(client.getThisNode());
		
		//Reset the server ip
		client.setServerIP(null);
		
		if(client.bindRMIservice(this, "Bootstrap_" + client.getHostname()))
		{			
			if(sendDiscoveryMulticast())
			{
				timeOut.startTimer();												//Start time out detection
				return true;
			}
			else
			{
				client.unbindRMIservice("Bootstrap_" + client.getHostname());		//Undo the made RMI service
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Sends out a multicast message when entering network.
	 * @return boolean	True if the function succeeded without errors, false if not.
	 */
	private boolean sendDiscoveryMulticast()
	{
		byte[] discoveryMessage = new String(client.getHostname() + " " + client.getIP()).getBytes();
		return client.sendMulticast(discoveryMessage);
	}
	
	/**
	 * Finish bootstrap services and enter 'running' mode.
	 */
	private synchronized void finishBootstrap()
	{
		if(serverRespons && (prevNodeRespons || firstNetworkNode))
		{
			timeOut.stopTimer();
			client.unbindRMIservice("Bootstrap_" + client.getHostname());
			
			if(firstNetworkNode)			//First node creates the file agent
			{
				client.setFileAgentMaster();
				client.createFileAgent();
			}
			else
			{
				if(client.getPrevNode().getHash() > client.getThisNode().getHash())		//This node becomes the new file agent master
				{
					client.assignFileAgentMaster(client.getNextNode(), false);
					client.setFileAgentMaster();
				}
			}
			
			if(client.runService())
			{
				client.getObserver().setChanged();
				client.getObserver().notifyObservers("Login");
			}
			else
			{
				client.loginFailed();
			}
		}
	}
	
	/**
	 * Detect a timeout: If the sessionstate of the client is false, the client will log out from the system.
	 */
	private void timeOutDetection()
	{
		if(!client.unbindRMIservice("Bootstrap_" + client.getHostname()))
		{
			client.printTerminalError("Bootstrap wasn't running.");
		}
		
		client.loginFailed();
		
		if(!serverRespons)
		{
			client.printTerminalError("Login time-out. Server did not respond.");
		}
		else
		{
			client.printTerminalError("Login time-out. Previous node did not respond.");
		}
	}
}
