package be.uantwerpen.systemY.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import be.uantwerpen.systemY.shared.Node;

/**
 * Interface to the BootstrapManager class of the Client.
 * @extends Remote
 */
public interface BootstrapManagerInterface extends Remote
{
	/**
	 * Sets the linked next and previous nodes to a client
	 * @param Node 	prevNode
	 * @param Node 	nextNode
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode) throws RemoteException;
	
	/**
	 * Give the server ip address and the network size to the new node.
	 * @param String	serverIP
	 * @param int	networkSize
	 */
	public void setNetwork(String serverIP, int networkSize) throws RemoteException;
}
