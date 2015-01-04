package be.uantwerpen.systemY.interfaces;

import java.rmi.*;
import be.uantwerpen.systemY.shared.Node;

/**
 * Interface to the NodeLinkManager class of a Client.
 */
public interface NodeLinkManagerInterface extends Remote
{
	/**
	 * Sets previous and next node.
	 * @param prevNode	The node that needs to be set as previous node.
	 * @param nextNode	The node that needs to be set as next node.
	 * @throws RemoteException
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode) throws RemoteException;
	
	/**
	 * Sets the next node.
	 * @param node	The node that needs to be set as next node.
	 * @throws RemoteException
	 */
	public void setNext(Node node) throws RemoteException;
	
	/**
	 * Sets previous node.
	 * @param node	The node that needs to be set as previous node.
	 * @throws RemoteException
	 */
	public void setPrev(Node node) throws RemoteException;
	
	/**
	 * Get the previous node.
	 * @return The previous node.
	 * @throws RemoteException
	 */
	public Node getPrev() throws RemoteException;
	
	/**
	 * Get the next node.
	 * @return The next node.
	 * @throws RemoteException
	 */
	public Node getNext() throws RemoteException;
}