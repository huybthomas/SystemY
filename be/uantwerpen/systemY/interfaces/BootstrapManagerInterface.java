package be.uantwerpen.systemY.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import be.uantwerpen.systemY.shared.Node;

/**
 * Interface to the BootstrapManager class of a Client.
 */
public interface BootstrapManagerInterface extends Remote
{
	/**
	 * Sets the linked next and previous nodes to a client.
	 * @param prevNode	The node to which the connection of previous node need to be made.
	 * @param nextNode	The node to which the connection of next node need to be made.
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode) throws RemoteException;
	
	/**
	 * Give the server ip address and the network size to the new node.
	 * @param serverIP		The ip address of the server.
	 * @param networkSize	The number of nodes in the network.
	 * @throws RemoteException.
	 */
	public void setNetwork(String serverIP, int networkSize) throws RemoteException;
}
