package be.uantwerpen.systemY.interfaces;

import java.rmi.*;
import be.uantwerpen.systemY.shared.Node;

/**
 * Interface to the NodeLinkManager class of the Client.
 * @extends Remote
 */
public interface NodeLinkManagerInterface extends Remote
{
	/**
	 * Sets previous and next node.
	 * @param prevNode	The node that needs to be set as previous node.
	 * @param nextNode	The node that needs to be set as next node.
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode) throws RemoteException;
	
	/**
	 * Sets the next node.
	 * @param node	The node that needs to be set as next node.
	 */
	public void setNext(Node node) throws RemoteException;
	
	/**
	 * Sets previous node.
	 * @param node	The node that needs to be set as previous node.
	 */
	public void setPrev(Node node) throws RemoteException;
	
	/**
	 * get the previous node.
	 * @return node
	 */
	public Node getPrev() throws RemoteException;
	
	/**
	 * get next node.
	 * @return node
	 */
	public Node getNext() throws RemoteException;
}