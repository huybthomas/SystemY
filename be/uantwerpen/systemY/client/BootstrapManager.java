package be.uantwerpen.systemY.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import be.uantwerpen.systemY.interfaces.BootstrapManagerInterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Class that handles the bootstrap of a client
 * @extends UnicastRemoteObject
 * @implements BootstrapManagerInterface
 */
public class BootstrapManager extends UnicastRemoteObject implements BootstrapManagerInterface
{
	private static final long serialVersionUID = 1L;
	private Client client;
	private boolean serverRespons, prevNodeRespons;
	private boolean firstNetworkNode;
	
	/**
	 * Create the BootstrapManager object.
	 * @param Client	the client
	 * @throws RemoteException
	 */
	public BootstrapManager(Client client) throws RemoteException
	{
		this.client = client;
	}
	
	/**
	 * Sets the linked next and previous nodes to a client
	 * @param Node 	prevNode
	 * @param Node 	nextNode
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode)
	{
		client.setLinkedNodes(prevNode, nextNode);
		prevNodeRespons = true;
		
		finishBootstrap();
	}
	
	/**
	 * Give the server ip address and the network size to the new node.
	 * @param String	serverIP
	 * @param int	networkSize
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
	 * Implements the bootstrap service
	 * @return boolean	True if succcess, false if not
	 */
	public boolean startBootstrap()
	{
		//Initialize bootstrap
		serverRespons = false;
		prevNodeRespons = false;
		firstNetworkNode = false;
		
		if(client.bindRMIservice(this, "Bootstrap_" + client.getHostname()))
		{
			if(sendDiscoveryMulticast())
			{
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
	 * Sends out a multicast message when entering network
	 * @return boolean	True if success, false if not
	 */
	private boolean sendDiscoveryMulticast()
	{
		byte[] discoveryMessage = new String(client.getHostname() + " " + client.getIP()).getBytes();
		return client.sendMulticast(discoveryMessage);
	}
	
	/**
	 * Finish bootstrap services and enter 'running' mode
	 */
	private void finishBootstrap()
	{
		if(serverRespons && (prevNodeRespons || firstNetworkNode))
		{
			client.unbindRMIservice("Bootstrap_" + client.getHostname());
			client.runServices();
			client.setSessionState(true);
		}
	}
}
