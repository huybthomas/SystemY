package be.uantwerpen.systemY.interfaces;

import java.rmi.*;
import be.uantwerpen.systemY.shared.Node;


/**
 * Interface to the NodeManager class of the Server.
 */
public interface NodeManagerInterface extends Remote
{
	/**
	 * Adds a Node to the NodeList.
	 * @param hostname	The name of the node that needs to be added.
	 * @param ipAddress	The ip of the node that needs to be added.
	 * @return True if successful, false otherwise.
	 */
	public boolean addNode(String hostname, String ipAddress) throws RemoteException;
	
	/**
	 * Delete the given node from the NodeList.
	 * @param hostname	The name of the node that needs to be deleted.
	 * @return	True if successful, false otherwise.
	 */
	public boolean delNode(String hostname) throws RemoteException;
	
	/**
	 * Get ip address of a given hostname.
	 * @param hostname	The name of the node you want
	 * @return The ip address of the requested node.
	 */
	public String getNode(String hostname) throws RemoteException;
	
	/**
	 * Returns the node where the file can be found.
	 * @param filename	The name of the requested file.
	 * @return	The node on which the file is located.
	 */
	public Node getFileLocation(String filename) throws RemoteException;
	
	/**
	 * Sends an answer to the new node.
	 * @param hostname	The name of the node you want.
	 * @return The next node of the host.
	 */
	public Node getNextNode(String hostname) throws RemoteException;
	
	/**
	 * Sends an answer to the new node.
	 * @param hostname	The name of the node you want.
	 * @return The previous node of the host.
	 */
	public Node getPrevNode(String hostname) throws RemoteException;
}
