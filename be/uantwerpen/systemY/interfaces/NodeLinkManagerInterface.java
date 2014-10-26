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
	 * @param Node	prevNode
	 * @param Node	nextNode
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode) throws RemoteException;
	
	/**
	 * Sets the next node.
	 * @param Node node
	 */
	public void setNext(Node node) throws RemoteException;
	
	/**
	 * Sets previous node.
	 * @param Node node
	 */
	public void setPrev(Node node) throws RemoteException;
}